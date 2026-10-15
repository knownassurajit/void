package com.knownassurajit.app.launcher.voidlauncher.ui.theme.font

import java.io.File

/**
 * Locates `src/main/res` for JVM unit tests that validate shipped resources.
 *
 * AGP runs unit tests with the project root as the working directory; walking up keeps the lookup
 * correct if the working directory is ever a sub-folder.
 */
internal object ProjectResources {
    private const val RES_DIR = "src/main/res"

    fun resDir(): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        while (dir != null) {
            val candidate = File(dir, RES_DIR)
            if (candidate.isDirectory) return candidate
            dir = dir.parentFile
        }
        error("Could not locate $RES_DIR from user.dir=${System.getProperty("user.dir")}")
    }
}
