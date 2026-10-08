package com.knownassurajit.app.launcher.voidlauncher.ui.gesture

import androidx.compose.ui.unit.IntSize
import com.knownassurajit.app.launcher.voidlauncher.R
import com.knownassurajit.app.launcher.voidlauncher.data.Prefs.SwipeAction
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavDirection
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavLayer
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavRouteKind
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.NavTravel
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.PanelEdge
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.VoidAlpha
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.ProjectResources
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.roundToInt

class LaunchAnimationTest {

    private val realEdges = listOf(PanelEdge.Left, PanelEdge.Right, PanelEdge.Top, PanelEdge.Bottom)

    private fun settings(left: String, right: String) = HomeSwipeSettings(
        gesturesEnabled = true,
        swipeDownEnabled = true,
        leftAction = left,
        rightAction = right,
        leftAppPackage = "left.app",
        rightAppPackage = "right.app",
        flags = SwipeFeatureFlags(notes = true, widgets = true, notificationSummary = true)
    )

    /** The animation of the app a swipe launches: the edge of the target, as `MainViewModel.launchSwipeApp` passes it on. */
    private fun launchOf(target: SwipeTarget): LaunchAnimation? = (target as SwipeTarget.App).edge.launchAnimation()

    // ---- edge -> animation ----

    @Test
    fun everyEdgeMapsToTheAnimationThatComesInFromIt() {
        assertEquals(LaunchAnimation.FromLeft, PanelEdge.Left.launchAnimation())
        assertEquals(LaunchAnimation.FromRight, PanelEdge.Right.launchAnimation())
        assertEquals(LaunchAnimation.FromTop, PanelEdge.Top.launchAnimation())
        assertEquals(LaunchAnimation.FromBottom, PanelEdge.Bottom.launchAnimation())
    }

    @Test
    fun aSwipeWithoutAnEdge_keepsTheDefaultWindowAnimation() {
        assertNull(PanelEdge.None.launchAnimation())
    }

    @Test
    fun theLeftSwipeActionComesInFromTheLeft_andTheRightOneFromTheRight() {
        val s = settings(left = SwipeAction.APP, right = SwipeAction.APP)
        assertEquals(LaunchAnimation.FromLeft, launchOf(s.targetFor(PanelEdge.Left)))
        assertEquals(LaunchAnimation.FromRight, launchOf(s.targetFor(PanelEdge.Right)))
    }

    @Test
    fun theFingerMovingRight_bringsTheAppInFromTheLeft_andTheFingerMovingLeftFromTheRight() {
        // Finger direction -> edge -> animation, the chain the Home swipe runs.
        assertEquals(LaunchAnimation.FromLeft, edgeForFinger(SwipeAxis.Horizontal, 50f).launchAnimation())
        assertEquals(LaunchAnimation.FromRight, edgeForFinger(SwipeAxis.Horizontal, -50f).launchAnimation())
        assertEquals(LaunchAnimation.FromBottom, edgeForFinger(SwipeAxis.Vertical, -50f).launchAnimation())
        assertEquals(LaunchAnimation.FromTop, edgeForFinger(SwipeAxis.Vertical, 50f).launchAnimation())
    }

    @Test
    fun swipeUpOpensTheDrawerFromTheBottom_andIsNotAnAppLaunch() {
        val target = settings(SwipeAction.APP, SwipeAction.APP).targetFor(PanelEdge.Bottom)
        assertEquals(SwipeTarget.Panel(NavRouteKind.AppDrawer, PanelEdge.Bottom), target)
        assertEquals(LaunchAnimation.FromBottom, PanelEdge.Bottom.launchAnimation())
    }

    // ---- where the system plays it ----

    @Test
    fun theCustomAnimation_isPlayedUpToAndroid11Only() {
        // Android 12 (API 31) ignores a custom animation for a launch that opens a new task.
        listOf(26, 28, 29, 30).forEach { assertTrue("API $it", LaunchAnimation.playsOn(it)) }
        listOf(31, 32, 33, 34, 35, 36, 37).forEach { assertFalse("API $it", LaunchAnimation.playsOn(it)) }
    }

    // ---- the resource pairs ----

    @Test
    fun everyAnimationUsesTheSlidePairOfItsEdge() {
        assertEquals(R.anim.slide_in_left, LaunchAnimation.FromLeft.enter)
        assertEquals(R.anim.slide_out_right, LaunchAnimation.FromLeft.exit)
        assertEquals(R.anim.slide_in_right, LaunchAnimation.FromRight.enter)
        assertEquals(R.anim.slide_out_left, LaunchAnimation.FromRight.exit)
        assertEquals(R.anim.slide_in_top, LaunchAnimation.FromTop.enter)
        assertEquals(R.anim.slide_out_bottom, LaunchAnimation.FromTop.exit)
        assertEquals(R.anim.slide_in_bottom, LaunchAnimation.FromBottom.enter)
        assertEquals(R.anim.slide_out_top, LaunchAnimation.FromBottom.exit)
    }

    // ---- the XML of the pairs ----

    private val files = mapOf(
        PanelEdge.Left to ("slide_in_left" to "slide_out_right"),
        PanelEdge.Right to ("slide_in_right" to "slide_out_left"),
        PanelEdge.Top to ("slide_in_top" to "slide_out_bottom"),
        PanelEdge.Bottom to ("slide_in_bottom" to "slide_out_top")
    )

    private fun root(file: File): Element =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).documentElement

    private fun anim(name: String): Element = root(File(ProjectResources.resDir(), "anim/$name.xml"))

    private fun Element.children(tag: String): List<Element> {
        val nodes = getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun Element.attr(name: String): String = getAttribute("android:$name")

    /** "-30%p" -> -30, "0" -> 0. */
    private fun percent(value: String): Float = value.removeSuffix("%p").toFloat()

    @Test
    fun everySlideAnimationRunsFor300msOnTheEmphasizedDecelerateCurve() {
        files.values.flatMap { listOf(it.first, it.second) }.forEach { name ->
            val steps = anim(name).children("translate") + anim(name).children("alpha")
            assertTrue(name, steps.isNotEmpty())
            steps.forEach { step ->
                assertEquals("$name ${step.tagName} duration", "300", step.attr("duration"))
                assertEquals(
                    "$name ${step.tagName} interpolator",
                    "@interpolator/emphasized_decelerate",
                    step.attr("interpolator")
                )
            }
        }
    }

    @Test
    fun theInterpolatorIsTheMaterial3EmphasizedDecelerateBezier() {
        val interpolator = root(File(ProjectResources.resDir(), "interpolator/emphasized_decelerate.xml"))
        assertEquals("pathInterpolator", interpolator.tagName)
        assertEquals(0.05f, interpolator.attr("controlX1").toFloat(), 0f)
        assertEquals(0.7f, interpolator.attr("controlY1").toFloat(), 0f)
        assertEquals(0.1f, interpolator.attr("controlX2").toFloat(), 0f)
        assertEquals(1.0f, interpolator.attr("controlY2").toFloat(), 0f)
    }

    @Test
    fun theIncomingAppTravelsItsWholeExtentFromItsEdgeWithoutAFade() {
        val screen = IntSize(1000, 1000)
        files.forEach { (edge, names) ->
            val incoming = anim(names.first)
            assertTrue("$edge has no fade", incoming.children("alpha").isEmpty())
            val translate = incoming.children("translate").single()
            val start = NavTravel.path(edge, NavLayer.Panel, NavDirection.Push, screen).start
            val horizontal = edge == PanelEdge.Left || edge == PanelEdge.Right
            val from = percent(translate.attr(if (horizontal) "fromXDelta" else "fromYDelta"))
            val to = percent(translate.attr(if (horizontal) "toXDelta" else "toYDelta"))
            // The same start as the panel transition: out of sight on its edge, the whole extent away.
            assertEquals("$edge from", (if (horizontal) start.x else start.y) / 10f, from, 0f)
            assertEquals("$edge to", 0f, to, 0f)
            // And nothing on the other axis.
            assertEquals("", translate.attr(if (horizontal) "fromYDelta" else "fromXDelta"))
        }
    }

    @Test
    fun homeLeavesTheSameWayTheAppComesInAndByTheParallaxOfThePanelTransition() {
        val screen = IntSize(1000, 1000)
        files.forEach { (edge, names) ->
            val outgoing = anim(names.second)
            val translate = outgoing.children("translate").single()
            val end = NavTravel.path(edge, NavLayer.Covered, NavDirection.Push, screen).end
            val horizontal = edge == PanelEdge.Left || edge == PanelEdge.Right
            val from = percent(translate.attr(if (horizontal) "fromXDelta" else "fromYDelta"))
            val to = percent(translate.attr(if (horizontal) "toXDelta" else "toYDelta"))
            // 30% of the width or 20% of the height, in the direction the covered screen moves.
            val expected = (if (horizontal) end.x else end.y) / 10f
            assertEquals("$edge from", 0f, from, 0f)
            assertEquals("$edge to", expected, to, 0f)
            val parallax = if (horizontal) NavTravel.horizontalParallax else NavTravel.verticalParallax
            assertEquals("$edge parallax", (parallax * 100).roundToInt().toFloat(), Math.abs(to), 0f)
        }
    }

    @Test
    fun homeDimsToTheCoveredOpacityWhileItLeaves() {
        files.forEach { (edge, names) ->
            val fade = anim(names.second).children("alpha").single()
            assertEquals("$edge from", 1.0f, fade.attr("fromAlpha").toFloat(), 0f)
            assertEquals("$edge to", VoidAlpha.coveredByPanel, fade.attr("toAlpha").toFloat(), 0f)
        }
    }

    @Test
    fun tappingAnAppKeepsTheDefaultFadeWindowAnimation() {
        val styles = File(ProjectResources.resDir(), "values/styles.xml").readText()
        assertTrue(styles.contains("<item name=\"android:windowEnterAnimation\">@anim/fade_enter</item>"))
        assertTrue(styles.contains("<item name=\"android:windowExitAnimation\">@anim/fade_exit</item>"))
    }
}
