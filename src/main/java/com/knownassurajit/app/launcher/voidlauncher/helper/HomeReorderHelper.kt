package com.knownassurajit.app.launcher.voidlauncher.helper

/**
 * Pure home-app reorder logic. Identity is stable across slots (package + user + shortcut).
 * Cascade moves shift neighbors so dragging slot 5 → 2 yields [A,E,B,C,D] from [A,B,C,D,E].
 */
object HomeReorderHelper {

    data class ReorderKey(
        val packageName: String,
        val userString: String,
        val isShortcut: Boolean,
        val shortcutId: String
    )

    fun <T> keyOf(
        item: T,
        packageName: (T) -> String,
        userString: (T) -> String,
        isShortcut: (T) -> Boolean,
        shortcutId: (T) -> String
    ): ReorderKey = ReorderKey(
        packageName = packageName(item),
        userString = userString(item),
        isShortcut = isShortcut(item),
        shortcutId = shortcutId(item)
    )

    /**
     * Applies vertical drag deltas against measured row heights, cascading across
     * multiple slots in a single frame via while-loops.
     *
     * @return Pair of (newList, newDraggedIndex, remainingDragY)
     */
    fun <T> applyDragCascade(
        list: List<T>,
        draggedIndex: Int,
        dragY: Float,
        itemHeights: FloatArray,
        keyOf: (T) -> ReorderKey
    ): Triple<List<T>, Int, Float> {
        if (draggedIndex !in list.indices || list.isEmpty()) {
            return Triple(list, draggedIndex, dragY)
        }

        val mutable = list.toMutableList()
        var index = draggedIndex
        var remaining = dragY
        val draggedKey = keyOf(mutable[index])

        while (remaining > 0f && index < mutable.lastIndex) {
            val neighborHeight = heightAt(itemHeights, index + 1, heightAt(itemHeights, index, 1f))
            val threshold = neighborHeight * 0.5f
            if (remaining <= threshold) break
            val from = index
            val moved = mutable.removeAt(from)
            mutable.add(from + 1, moved)
            index = from + 1
            remaining -= neighborHeight
            // Re-sync index if list identity drifted (should not happen)
            val synced = mutable.indexOfFirst { keyOf(it) == draggedKey }
            if (synced >= 0) index = synced
        }

        while (remaining < 0f && index > 0) {
            val neighborHeight = heightAt(itemHeights, index - 1, heightAt(itemHeights, index, 1f))
            val threshold = neighborHeight * 0.5f
            if (-remaining <= threshold) break
            val from = index
            val moved = mutable.removeAt(from)
            mutable.add(from - 1, moved)
            index = from - 1
            remaining += neighborHeight
            val synced = mutable.indexOfFirst { keyOf(it) == draggedKey }
            if (synced >= 0) index = synced
        }

        return Triple(mutable.toList(), index, remaining)
    }

    /**
     * Moves [fromIndex] to [toIndex], shifting neighbors (insert-style cascade).
     * Useful for tests and direct programmatic reorder.
     */
    fun <T> moveItem(list: List<T>, fromIndex: Int, toIndex: Int): List<T> {
        if (fromIndex !in list.indices || toIndex !in list.indices || fromIndex == toIndex) {
            return list
        }
        val mutable = list.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        return mutable.toList()
    }

    private fun heightAt(heights: FloatArray, index: Int, fallback: Float): Float {
        if (index !in heights.indices) return fallback.coerceAtLeast(1f)
        val h = heights[index]
        return if (h > 0f) h else fallback.coerceAtLeast(1f)
    }
}
