package com.knownassurajit.app.launcher.voidlauncher.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first

/**
 * The arithmetic of a header that is hidden until the user pulls down. `offset` is how far it is
 * out, in pixels: 0 is hidden, `height` is fully shown. Pure, so every rule is JVM-testable.
 */
object HeaderRevealMath {
    /** [offset] kept between hidden (0) and fully shown ([height]). */
    fun clamp(offset: Float, height: Float): Float = offset.coerceIn(0f, height.coerceAtLeast(0f))

    /** The share of the header that is out, 0 (hidden) to 1 (fully shown); 0 while the height is still unknown. */
    fun fraction(offset: Float, height: Float): Float =
        if (height <= 0f) 0f else (offset / height).coerceIn(0f, 1f)

    /**
     * Where a release settles. A fling decides on its own: moving down at [flingVelocity] or faster
     * opens the header, moving up that fast closes it. A slower release keeps it open when at least
     * half of it is out, and closes it otherwise. [velocity] is the finger's, positive downward, in px/s.
     */
    fun settleTarget(offset: Float, height: Float, velocity: Float, flingVelocity: Float): Float = when {
        height <= 0f -> 0f
        velocity >= flingVelocity -> height
        velocity <= -flingVelocity -> 0f
        offset >= height / 2f -> height
        else -> 0f
    }
}

/**
 * How far a header that is hidden by default is out. Read [offsetPx] only in a layer or draw block
 * (`graphicsLayer { }`): it changes on every frame of a pull and of the spring that settles it, and
 * as a composition read it would recompose the screen each time.
 */
@Stable
class RevealHeaderState {
    /** Height of the header in pixels; 0 until it has been measured. */
    var heightPx: Float by mutableFloatStateOf(0f)
        private set

    /** How much of the header is out, in pixels. */
    var offsetPx: Float by mutableFloatStateOf(0f)
        private set

    val fraction: Float get() = HeaderRevealMath.fraction(offsetPx, heightPx)
    val isHidden: Boolean get() = offsetPx <= 0f
    val isFullyShown: Boolean get() = heightPx > 0f && offsetPx >= heightPx

    internal fun onMeasured(height: Float) {
        if (height == heightPx) return
        heightPx = height
        offsetPx = HeaderRevealMath.clamp(offsetPx, height)
    }

    /** Moves the header by [deltaPx] (positive reveals it) and returns how much of that it took. */
    fun dragBy(deltaPx: Float): Float {
        val next = HeaderRevealMath.clamp(offsetPx + deltaPx, heightPx)
        val applied = next - offsetPx
        offsetPx = next
        return applied
    }

    private suspend fun animateTo(target: Float, spec: AnimationSpec<Float>, velocity: Float = 0f) {
        // Already resting at hidden or fully shown: a fling that points further out would only spring against the clamp.
        if (target == offsetPx && (velocity == 0f || target <= 0f || target >= heightPx)) return
        animate(initialValue = offsetPx, targetValue = target, initialVelocity = velocity, animationSpec = spec) { value, _ ->
            offsetPx = HeaderRevealMath.clamp(value, heightPx)
        }
    }

    /** Springs the header fully out. Waits for the first measurement, so it can be asked for before the first frame. */
    suspend fun show(spec: AnimationSpec<Float>) {
        snapshotFlow { heightPx }.first { it > 0f }
        animateTo(heightPx, spec)
    }

    /** Springs the header away. */
    suspend fun hide(spec: AnimationSpec<Float>) = animateTo(0f, spec)

    /** Springs to where a release at [velocity] (the finger's, px/s, positive downward) belongs; see [HeaderRevealMath.settleTarget]. */
    suspend fun settle(velocity: Float, flingVelocity: Float, spec: AnimationSpec<Float>) {
        animateTo(HeaderRevealMath.settleTarget(offsetPx, heightPx, velocity, flingVelocity), spec, velocity)
    }
}

/**
 * Pulls [state]'s header in as the finger moves down and pushes it away as the finger moves up, the
 * way a Material "enter always" app bar does, but without taking any scroll from the content: the
 * header floats over it, so the list answers the finger at once. Releasing springs the header to
 * open or closed ([HeaderRevealMath.settleTarget]).
 *
 * While the header is still coming out, a pull the list could not use is taken here, so the list
 * does not stretch as well; once the header is fully out the rest of the pull stretches the list.
 * [isEnabled] is read on every event, so the header can be switched off (editing, picker open).
 */
class RevealHeaderConnection(
    private val state: RevealHeaderState,
    private val flingVelocityPx: Float,
    private val settleSpec: () -> AnimationSpec<Float>,
    private val isEnabled: () -> Boolean
) : NestedScrollConnection {
    private var releaseVelocity = 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (isEnabled() && available.y != 0f) state.dragBy(available.y)
        return Offset.Zero
    }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        if (isEnabled() && source == NestedScrollSource.UserInput && available.y > 0f && !state.isFullyShown) {
            Offset(0f, available.y)
        } else {
            Offset.Zero
        }

    override suspend fun onPreFling(available: Velocity): Velocity {
        releaseVelocity = available.y
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val velocity = releaseVelocity
        releaseVelocity = 0f
        if (isEnabled()) state.settle(velocity, flingVelocityPx, settleSpec())
        return Velocity.Zero
    }
}

/**
 * Places a header that [state] slides in from above the top edge: it is laid out at the top and
 * moved by [RevealHeaderState.offsetPx] in the layer, so showing it never re-lays out what is under
 * it. It is invisible while hidden, which also covers the frames before it has been measured.
 */
fun Modifier.revealHeader(state: RevealHeaderState): Modifier = this
    .onSizeChanged { state.onMeasured(it.height.toFloat()) }
    .graphicsLayer {
        // Nothing is drawn while it is hidden, so not even its shadow shows at the top edge.
        alpha = if (state.offsetPx > 0f) 1f else 0f
        translationY = state.offsetPx - state.heightPx
    }

/**
 * The small bar that says a header is waiting above: it fades out as the header comes in, a tap
 * toggles the header ([onToggle]) and a drag pulls it. The target is wider than the bar so that a
 * thumb finds it; [description] is what a screen reader says.
 */
@Composable
fun RevealHandle(
    state: RevealHeaderState,
    description: String,
    flingVelocityPx: Float,
    settleSpec: AnimationSpec<Float>,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dragState = rememberDraggableState { delta -> state.dragBy(delta) }
    Box(
        modifier = modifier
            .size(width = HANDLE_TARGET_WIDTH, height = HANDLE_TARGET_HEIGHT)
            .graphicsLayer { alpha = 1f - state.fraction }
            .draggable(
                state = dragState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity -> state.settle(velocity, flingVelocityPx, settleSpec) }
            )
            // No ripple: the bar is a hint, and the header moving is the feedback.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = description,
                role = Role.Button,
                onClick = onToggle
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = HANDLE_BOTTOM_GAP)
                .size(width = HANDLE_BAR_WIDTH, height = HANDLE_BAR_HEIGHT)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HANDLE_ALPHA), CircleShape)
        )
    }
}

private val HANDLE_TARGET_WIDTH = 96.dp
private val HANDLE_TARGET_HEIGHT = 44.dp
private val HANDLE_BAR_WIDTH = 36.dp
private val HANDLE_BAR_HEIGHT = 4.dp
private val HANDLE_BOTTOM_GAP = 6.dp
private const val HANDLE_ALPHA = 0.5f
