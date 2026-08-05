package com.knownassurajit.app.launcher.voidlauncher.helper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeReorderHelperTest {

    private data class Item(val id: String)

    private fun keyOf(item: Item) = HomeReorderHelper.ReorderKey(
        packageName = item.id,
        userString = "0",
        isShortcut = false,
        shortcutId = ""
    )

    @Test
    fun moveItem_fromLastToSecond_cascadesNeighbors() {
        val list = listOf(Item("A"), Item("B"), Item("C"), Item("D"), Item("E"))
        val result = HomeReorderHelper.moveItem(list, fromIndex = 4, toIndex = 1)
        assertEquals(listOf("A", "E", "B", "C", "D"), result.map { it.id })
    }

    @Test
    fun applyDragCascade_crossesMultipleSlotsDownward() {
        val list = listOf(Item("A"), Item("B"), Item("C"), Item("D"), Item("E"))
        val heights = FloatArray(5) { 100f }
        // Drag item E (index 4) upward enough to cross 3 half-heights toward index 1
        val (newList, newIndex, _) = HomeReorderHelper.applyDragCascade(
            list = list,
            draggedIndex = 4,
            dragY = -280f,
            itemHeights = heights,
            keyOf = ::keyOf
        )
        assertTrue(newIndex <= 2)
        assertEquals("E", newList[newIndex].id)
        assertEquals(5, newList.size)
        assertEquals(setOf("A", "B", "C", "D", "E"), newList.map { it.id }.toSet())
    }

    @Test
    fun applyDragCascade_movesDownAndShiftsNeighbors() {
        val list = listOf(Item("A"), Item("B"), Item("C"))
        val heights = FloatArray(3) { 100f }
        // One half-height threshold crossed (100 * 0.5 = 50) → single adjacent swap
        val (newList, newIndex, _) = HomeReorderHelper.applyDragCascade(
            list = list,
            draggedIndex = 0,
            dragY = 60f,
            itemHeights = heights,
            keyOf = ::keyOf
        )
        assertEquals(1, newIndex)
        assertEquals(listOf("B", "A", "C"), newList.map { it.id })
    }

    @Test
    fun applyDragCascade_multiSlotDown_fromFirstToLast() {
        val list = listOf(Item("A"), Item("B"), Item("C"))
        val heights = FloatArray(3) { 100f }
        val (newList, newIndex, _) = HomeReorderHelper.applyDragCascade(
            list = list,
            draggedIndex = 0,
            dragY = 160f,
            itemHeights = heights,
            keyOf = ::keyOf
        )
        assertEquals(2, newIndex)
        assertEquals(listOf("B", "C", "A"), newList.map { it.id })
    }
}
