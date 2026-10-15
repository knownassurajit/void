package com.knownassurajit.app.launcher.voidlauncher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Test

class TabularFiguresTest {

    @Test
    fun withTabularFigures_requestsTheTnumFeature() {
        assertEquals("tnum", TextStyle().withTabularFigures().fontFeatureSettings)
    }

    @Test
    fun withTabularFigures_keepsTheRestOfTheStyle() {
        val base = TextStyle(fontWeight = FontWeight.Bold, fontSize = 57.sp, letterSpacing = (-1.5).sp)

        val tabular = base.withTabularFigures()

        assertEquals(base.fontWeight, tabular.fontWeight)
        assertEquals(base.fontSize, tabular.fontSize)
        assertEquals(base.letterSpacing, tabular.letterSpacing)
    }
}
