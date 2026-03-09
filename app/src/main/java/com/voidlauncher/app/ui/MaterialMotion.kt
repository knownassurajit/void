package com.voidlauncher.app.ui

import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis

/**
 * Applies Material motion with sensible defaults so each screen feels consistent.
 *
 * Think of this as our "set and forget" transition preset: each fragment can call it
 * once in onCreate(), and we get polished movement without copy/paste boilerplate.
 */
fun Fragment.applyMaterialScreenTransitions(forwardAxis: Int = MaterialSharedAxis.Z) {
    enterTransition = MaterialSharedAxis(forwardAxis, true).apply { duration = 250L }
    returnTransition = MaterialSharedAxis(forwardAxis, false).apply { duration = 200L }
    reenterTransition = MaterialFadeThrough().apply { duration = 180L }
    exitTransition = MaterialFadeThrough().apply { duration = 160L }
}
