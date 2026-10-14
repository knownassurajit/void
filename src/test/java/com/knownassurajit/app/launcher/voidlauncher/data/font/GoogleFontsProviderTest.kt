package com.knownassurajit.app.launcher.voidlauncher.data.font

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoogleFontsProviderTest {

    @Test
    fun onlyPlayServicesMayOwnTheProvider() {
        assertTrue(GoogleFontsProvider.isOwnedByPlayServices("com.google.android.gms"))
        assertFalse(GoogleFontsProvider.isOwnedByPlayServices(null))
        assertFalse(GoogleFontsProvider.isOwnedByPlayServices(""))
        assertFalse(GoogleFontsProvider.isOwnedByPlayServices("org.microg.gms"))
        assertFalse(GoogleFontsProvider.isOwnedByPlayServices("com.google.android.gms.fake"))
    }

    @Test
    fun isAvailable_followsTheOwnerOfTheAuthority() {
        assertTrue(GoogleFontsProvider.isAvailable({ GoogleFontsProvider.PACKAGE }))
        assertFalse("no provider registered", GoogleFontsProvider.isAvailable({ null }))
        assertFalse("look-alike owner", GoogleFontsProvider.isAvailable({ "org.microg.gms" }))
    }

    @Test
    fun isAvailable_isFalseAndReportsWhenTheLookupThrows() {
        val errors = mutableListOf<Exception>()

        val available = GoogleFontsProvider.isAvailable(
            ownerOf = { throw SecurityException("package visibility") },
            onError = { errors += it }
        )

        assertFalse(available)
        assertEquals(1, errors.size)
        assertTrue(errors.single() is SecurityException)
    }
}
