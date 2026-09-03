package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

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

// MD3 Expressive — recommended shape tokens
private val VoidExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun VoidAppTheme(
    appFont: String = "inter",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            dynamicDarkColorScheme(context)
        else -> VoidDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(appFont),
        shapes = VoidExpressiveShapes,
        content = content
    )
}
