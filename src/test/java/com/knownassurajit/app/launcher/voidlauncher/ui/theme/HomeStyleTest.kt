package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeStyleTest {

    @Test
    fun opacity_isClampedAndRecoversFromCorruptValues() {
        assertEquals(HomeStyle.MIN_OPACITY, HomeStyle.coerceOpacity(0f), 0f)
        assertEquals(HomeStyle.MAX_OPACITY, HomeStyle.coerceOpacity(3f), 0f)
        assertEquals(HomeStyle.MAX_OPACITY, HomeStyle.coerceOpacity(Float.NaN), 0f)
        assertEquals(0.5f, HomeStyle.coerceOpacity(0.5f), 0f)
    }

    @Test
    fun sidePadding_isClampedAndRecoversFromCorruptValues() {
        assertEquals(HomeStyle.MIN_SIDE_PADDING_DP, HomeStyle.coerceSidePadding(-4f), 0f)
        assertEquals(HomeStyle.MAX_SIDE_PADDING_DP, HomeStyle.coerceSidePadding(400f), 0f)
        assertEquals(HomeStyle.DEFAULT_SIDE_PADDING_DP, HomeStyle.coerceSidePadding(Float.NaN), 0f)
    }

    @Test
    fun weights_outsideTheBundledSetFallBackToTheDefault() {
        assertEquals(500, HomeStyle.coerceWeight(500, HomeStyle.DEFAULT_APP_WEIGHT))
        assertEquals(HomeStyle.DEFAULT_CLOCK_WEIGHT, HomeStyle.coerceWeight(300, HomeStyle.DEFAULT_CLOCK_WEIGHT))
    }

    @Test
    fun appSpacing_matchesTheFormulaHomeUsedBefore() {
        // Before the shared style Home computed: base = clamp(h * 0.75 / (n + 3), 4, 32) * setting / 24.
        assertEquals(16f * 32f / 24f, HomeStyle.appSpacingDp(screenHeightDp = 800, displayedCount = 5, appSpacingSetting = 16f), 0.001f)
        assertEquals(0f, HomeStyle.appSpacingDp(800, 5, 0f), 0f)
        assertEquals(32f * 2f, HomeStyle.appSpacingDp(4000, 1, 48f), 0.001f)
    }

    @Test
    fun colors_unknownKeysFallBackToDefault() {
        assertEquals(HomeTextColor.Tertiary, HomeTextColor.fromKey("tertiary"))
        assertEquals(HomeTextColor.Default, HomeTextColor.fromKey("#FF00FF"))
        assertEquals(HomeTextColor.Default, HomeTextColor.fromKey(null))
    }

    @Test
    fun hex_acceptsShortLongAndAlphaFormsWithOrWithoutHash() {
        assertEquals(0xFFFFFFFF.toInt(), HomeColors.parseHex("#fff"))
        assertEquals(0xFF336699.toInt(), HomeColors.parseHex("336699"))
        assertEquals(0x80336699.toInt(), HomeColors.parseHex("#80336699"))
        assertEquals(null, HomeColors.parseHex("#12345"))
        assertEquals(null, HomeColors.parseHex("#GGGGGG"))
        assertEquals(null, HomeColors.parseHex(""))
        assertEquals(null, HomeColors.parseHex(null))
    }

    @Test
    fun hex_formatsOpaqueColorsWithoutAlpha() {
        assertEquals("#336699", HomeColors.toHex(0xFF336699.toInt()))
        assertEquals("#80336699", HomeColors.toHex(0x80336699.toInt()))
    }

    @Test
    fun hsv_roundTripsPrimaryAndGrayColors() {
        listOf(0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFF808080, 0xFFFFFFFF, 0xFF000000, 0xFF336699).forEach { raw ->
            val argb = raw.toInt()
            val hsv = HomeColors.argbToHsv(argb)
            assertEquals(HomeColors.toHex(argb), HomeColors.toHex(HomeColors.hsvToArgb(hsv[0], hsv[1], hsv[2])))
        }
    }

    @Test
    fun homeColor_storesRolesByKeyAndCustomColorsAsHex() {
        assertEquals(HomeColor.Role(HomeTextColor.Primary), HomeColor.parse("primary"))
        assertEquals(HomeColor.Custom(0xFF90CAF9.toInt()), HomeColor.parse("#90CAF9"))
        assertEquals("#90CAF9", HomeColor.parse("#90caf9").key)
        assertEquals(HomeColor.Default, HomeColor.parse("not-a-color"))
    }
}
