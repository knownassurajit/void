package com.knownassurajit.app.launcher.voidlauncher.data.font

/** Identity of the Google Play services downloadable-fonts provider (the certificates live in `font_certs.xml`). */
object GoogleFontsProvider {
    const val AUTHORITY = "com.google.android.gms.fonts"
    const val PACKAGE = "com.google.android.gms"

    /** A provider registered under [AUTHORITY] is only usable when Google Play services itself owns it. */
    fun isOwnedByPlayServices(owningPackage: String?): Boolean = owningPackage == PACKAGE

    /**
     * Whether the provider can be used, given a lookup of the package that owns [AUTHORITY]. A lookup that throws
     * means unavailable: the error goes to [onError] and the families that need the provider are hidden.
     */
    fun isAvailable(ownerOf: () -> String?, onError: (Exception) -> Unit = {}): Boolean =
        try {
            isOwnedByPlayServices(ownerOf())
        } catch (error: Exception) {
            onError(error)
            false
        }
}
