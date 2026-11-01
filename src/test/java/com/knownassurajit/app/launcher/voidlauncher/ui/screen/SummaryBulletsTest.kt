package com.knownassurajit.app.launcher.voidlauncher.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryBulletsTest {

    @Test
    fun `null or blank text has no points`() {
        assertEquals(emptyList<String>(), SummaryBullets.parse(null))
        assertEquals(emptyList<String>(), SummaryBullets.parse("  \n \n"))
    }

    @Test
    fun `one plain line is one point`() {
        assertEquals(listOf("Your parcel arrives today"), SummaryBullets.parse("Your parcel arrives today"))
    }

    @Test
    fun `each bullet marker is dropped and the text kept`() {
        val text = "• Code 123456 for sign-in\n* Meeting moved to 3 pm\n- Payment of $42 received"
        assertEquals(
            listOf("Code 123456 for sign-in", "Meeting moved to 3 pm", "Payment of $42 received"),
            SummaryBullets.parse(text)
        )
    }

    @Test
    fun `blank lines and surrounding spaces are ignored`() {
        assertEquals(listOf("First", "Second"), SummaryBullets.parse("  • First  \n\n   •   Second\n•  "))
    }

    @Test
    fun `a hyphen inside a point is not taken for a marker`() {
        assertEquals(listOf("Check-in opens at 9", "Re-sent the file"), SummaryBullets.parse("Check-in opens at 9\nRe-sent the file"))
    }

    @Test
    fun `a sign or emphasis that touches the text is kept`() {
        assertEquals(listOf("-5 degrees tonight", "*Urgent* reply needed"), SummaryBullets.parse("-5 degrees tonight\n*Urgent* reply needed"))
    }

    @Test
    fun `windows line endings split like plain ones`() {
        assertEquals(listOf("One", "Two"), SummaryBullets.parse("• One\r\n• Two"))
    }
}
