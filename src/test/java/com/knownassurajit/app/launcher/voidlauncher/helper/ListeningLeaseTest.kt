package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Test

class ListeningLeaseTest {

    private class Listener(count: ListenerCount) {
        var starts = 0
        var stops = 0
        val lease = ListeningLease(count, start = { starts++ }, stop = { stops++ })
    }

    private val count = ListenerCount()

    @Test
    fun aSingleHolder_startsOnAcquireAndStopsOnRelease() {
        val a = Listener(count)
        a.lease.acquire()
        assertEquals(1, a.starts)
        assertEquals(0, a.stops)
        a.lease.release()
        assertEquals(1, a.stops)
    }

    @Test
    fun theClosingScreenDoesNotStopTheNewestOne() {
        // Regression: the Widgets screen that is closing stays composed until its exit animation ends.
        // Opening Widgets again in that time started a second host, and the first one's stop then
        // silenced it (Android keeps one registration per host id).
        val closing = Listener(count)
        val reopened = Listener(count)
        closing.lease.acquire()
        reopened.lease.acquire()
        closing.lease.release()
        assertEquals(0, closing.stops)
        assertEquals(0, reopened.stops)
        reopened.lease.release()
        assertEquals(1, reopened.stops)
        assertEquals(0, closing.stops)
    }

    @Test
    fun everyAcquireStartsSoTheNewestHolderReceivesTheUpdates() {
        val first = Listener(count)
        val second = Listener(count)
        first.lease.acquire()
        second.lease.acquire()
        assertEquals(1, first.starts)
        assertEquals(1, second.starts)
    }

    @Test
    fun theLastHolderStopsWhateverTheOrderOfRelease() {
        val first = Listener(count)
        val second = Listener(count)
        first.lease.acquire()
        second.lease.acquire()
        second.lease.release()
        assertEquals(0, second.stops)
        first.lease.release()
        assertEquals(1, first.stops)
    }

    @Test
    fun releasingTwice_stopsOnceAndDoesNotStealAnotherHoldersClaim() {
        // The screen releases when its effect is disposed and again when its ViewModel is cleared.
        val a = Listener(count)
        val b = Listener(count)
        a.lease.acquire()
        b.lease.acquire()
        a.lease.release()
        a.lease.release()
        assertEquals(0, a.stops)
        assertEquals(0, b.stops)
        b.lease.release()
        assertEquals(1, b.stops)
    }

    @Test
    fun acquiringTwice_startsOnceAndOneReleaseStops() {
        val a = Listener(count)
        a.lease.acquire()
        a.lease.acquire()
        assertEquals(1, a.starts)
        a.lease.release()
        assertEquals(1, a.stops)
    }

    @Test
    fun releasingWithoutHavingAcquired_doesNothing() {
        val holder = Listener(count)
        val bystander = Listener(count)
        holder.lease.acquire()
        bystander.lease.release()
        assertEquals(0, bystander.stops)
        assertEquals(0, holder.stops)
        holder.lease.release()
        assertEquals(1, holder.stops)
    }

    @Test
    fun aLeaseCanBeAcquiredAgainAfterItWasReleased() {
        val a = Listener(count)
        a.lease.acquire()
        a.lease.release()
        a.lease.acquire()
        assertEquals(2, a.starts)
        a.lease.release()
        assertEquals(2, a.stops)
    }

    @Test
    fun separateCounts_doNotInfluenceEachOther() {
        val other = ListenerCount()
        val a = Listener(count)
        val b = Listener(other)
        a.lease.acquire()
        b.lease.acquire()
        a.lease.release()
        assertEquals(1, a.stops)
        assertEquals(0, b.stops)
    }
}
