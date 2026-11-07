package br.com.mural.core.date

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class DateUtilsTest {

    @Test
    fun `toDateKey formats ISO YYYY-MM-DD correctly`() {
        val date = LocalDate.of(2026, 11, 12)
        val key = DateUtils.toDateKey(date)
        assertEquals("2026-11-12", key)
    }

    @Test
    fun `parseDateKey parses ISO string correctly`() {
        val key = "2026-11-12"
        val date = DateUtils.parseDateKey(key)
        assertEquals(LocalDate.of(2026, 11, 12), date)
    }

    @Test
    fun `getMonthCells produces exactly 42 cells for November 2026`() {
        val month = YearMonth.of(2026, 11)
        val cells = DateUtils.getMonthCells(month, firstDayOfWeek = DayOfWeek.SUNDAY)

        assertEquals(42, cells.size)

        // Nov 1, 2026 is Sunday -> lead offset is 0
        assertEquals(LocalDate.of(2026, 11, 1), cells[0])
        assertEquals(LocalDate.of(2026, 11, 30), cells[29])

        // Remaining 12 cells (30 to 41) are null trailing cells
        assertNull(cells[30])
        assertNull(cells[41])
    }

    @Test
    fun `getMonthCells handles leap years correctly for February 2024`() {
        val month = YearMonth.of(2024, 2)
        val cells = DateUtils.getMonthCells(month, firstDayOfWeek = DayOfWeek.SUNDAY)

        assertEquals(42, cells.size)
        // Feb 1, 2024 is Thursday -> lead offset is 4 (Sun, Mon, Tue, Wed)
        assertNull(cells[0])
        assertNull(cells[3])
        assertEquals(LocalDate.of(2024, 2, 1), cells[4])
        assertEquals(LocalDate.of(2024, 2, 29), cells[32])
    }
}
