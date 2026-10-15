package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.knownassurajit.app.launcher.voidlauncher.data.font.FontCatalog
import com.knownassurajit.app.launcher.voidlauncher.ui.theme.font.rememberFontResolution

private val VoidDarkColorScheme = darkColorScheme(
    primary = VoidPrimary,
    onPrimary = VoidOnPrimary,
    primaryContainer = VoidPrimaryContainer,
    onPrimaryContainer = VoidOnPrimaryContainer,
    secondary = VoidSecondary,
    onSecondary = VoidOnSecondary,
    secondaryContainer = VoidSecondaryContainer,
    onSecondaryContainer = VoidOnSecondaryContainer,
    tertiary = VoidTertiary,
    onTertiary = VoidOnTertiary,
    tertiaryContainer = VoidTertiaryContainer,
    onTertiaryContainer = VoidOnTertiaryContainer,
    error = VoidError,
    onError = VoidOnError,
    errorContainer = VoidErrorContainer,
    onErrorContainer = VoidOnErrorContainer,
    background = VoidBlack,
    onBackground = VoidOnSurface,
    surface = VoidSurface,
    onSurface = VoidOnSurface,
    surfaceVariant = VoidSurfaceContainer,
    onSurfaceVariant = VoidOnSurfaceVariant,
    outline = VoidOutline,
    outlineVariant = VoidOutlineVariant,
    inverseSurface = VoidInverseSurface,
    inverseOnSurface = VoidInverseOnSurface,
    inversePrimary = VoidInversePrimary,
    scrim = VoidScrim,
    surfaceContainerHighest = VoidSurfaceContainerHighest,
    surfaceContainerHigh = VoidSurfaceContainerHigh,
    surfaceContainer = VoidSurfaceContainer,
    surfaceContainerLow = VoidSurfaceContainerLow,
    surfaceBright = VoidSurfaceContainerHighest,
    surfaceDim = VoidBlack
)

// Material 3 Expressive shape scale (corner radii in dp: 4, 8, 12, 16, 20, 28, 32, 48).
private val VoidExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
    largeIncreased = RoundedCornerShape(20.dp),
    extraLargeIncreased = RoundedCornerShape(32.dp),
    extraExtraLarge = RoundedCornerShape(48.dp)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VoidAppTheme(
    appFont: String = FontCatalog.DEFAULT_ID,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicDarkColorScheme(context)
        else -> VoidDarkColorScheme
    }

    val font = rememberFontResolution(appFont).spec
    val typography = remember(font) { getTypography(font) }

    // Expressive motion drives component springs (buttons, toggles, list items, indicators). Full-screen
    // panel transitions keep the standard springs in VoidMotion: an overshoot there reveals the screen edge.
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        shapes = VoidExpressiveShapes,
        typography = typography,
        content = content
    )
}
