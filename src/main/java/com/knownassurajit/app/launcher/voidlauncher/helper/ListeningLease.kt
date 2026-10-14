package com.knownassurajit.app.launcher.voidlauncher.helper

/**
 * Counts the holders of one listener that exists once per process, such as the `AppWidgetHost` with
 * a given id: Android keeps a single registration for it, so the instance that stops listening
 * silences every other instance. One [ListenerCount] must be shared by all [ListeningLease]s of the
 * same listener.
 */
class ListenerCount {
    private var holders = 0

    @Synchronized
    internal fun add() {
        holders++
    }

    /** Takes a holder away and says whether it was the last one. Only a lease that [add]ed itself calls it. */
    @Synchronized
    internal fun remove(): Boolean {
        holders--
        return holders == 0
    }
}

/**
 * One screen's claim on a listener that other screens can claim at the same time. [start] runs on
 * every [acquire], so the newest holder is the one that receives the updates; [stop] runs only when
 * the last holder [release]s. Without this, the closing Widgets screen, which stays composed until
 * its exit animation ends, stopped the listener of the Widgets screen that was opened again in the
 * meantime.
 *
 * Acquiring twice or releasing twice in a row does nothing, so it is safe to release from both a
 * disposed effect and `ViewModel.onCleared`. Not thread safe: use it from the main thread.
 */
class ListeningLease(
    private val count: ListenerCount,
    private val start: () -> Unit,
    private val stop: () -> Unit
) {
    private var held = false

    fun acquire() {
        if (held) return
        held = true
        count.add()
        start()
    }

    fun release() {
        if (!held) return
        held = false
        if (count.remove()) stop()
    }
}
