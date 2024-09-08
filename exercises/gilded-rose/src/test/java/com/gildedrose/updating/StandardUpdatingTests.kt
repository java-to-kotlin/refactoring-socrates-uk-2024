package com.gildedrose.updating

import com.gildedrose.Item
import com.gildedrose.oct29
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StandardUpdatingTests {

    @Test fun `items decrease in quality one per day`() {
        assertEquals(
            Item("banana", oct29, 41),
            Item("banana", oct29, 42).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 42),
            Item("banana", oct29, 42).updatedBy(days = 0, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 40),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `quality doesn't become negative`() {
        assertEquals(
            Item("banana", oct29, 0),
            Item("banana", oct29, 0).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 0),
            Item("banana", oct29, 1).updatedBy(days = 2, on = oct29)
        )
    }

    @Test fun `items decrease in quality two per day after sell by date`() {
        assertEquals(
            Item("banana", oct29, 40),
            Item("banana", oct29, 42).updatedBy(days = 1, on = oct29.plusDays(1))
        )
        assertEquals(
            Item("banana", oct29, 42),
            Item("banana", oct29, 42).updatedBy(days = 0, on = oct29.plusDays(1))
        )
        assertEquals(
            Item("banana", oct29, 38),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(2))
        )
        assertEquals(
            Item("banana", oct29, 39),
            Item("banana", oct29, 42).updatedBy(days = 2, on = oct29.plusDays(1))
        )
    }

    @Test fun `items with no sellBy don't change quality`() {
        assertEquals(
            Item("banana", null, 42),
            Item("banana", null, 42).updatedBy(days = 1, on = oct29)
        )
    }

    @Test fun `items with a quality above 50 degrade gradually`() {
        assertEquals(
            Item("banana", oct29, 51),
            Item("banana", oct29, 52).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 50),
            Item("banana", oct29, 51).updatedBy(days = 1, on = oct29)
        )
        assertEquals(
            Item("banana", oct29, 49),
            Item("banana", oct29, 51).updatedBy(days = 1, on = oct29.plusDays(1))
        )
    }
}
