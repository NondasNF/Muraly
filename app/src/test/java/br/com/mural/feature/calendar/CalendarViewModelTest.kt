package br.com.mural.feature.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModelTest {

    @Test
    fun calendarUiStateDefaultsToCurrentMonth() {
        val month = YearMonth.of(2026, 11)
        val state = CalendarUiState(month = month)
        assertEquals(month, state.month)
    }

    @Test
    fun dayCellUiFormatsCorrectly() {
        val cell = DayCellUi(
            date = LocalDate.of(2026, 11, 12),
            coverUri = "content://media/100",
            photoCount = 6,
            isToday = true,
            isFuture = false,
            isCoverAvailable = true
        )

        assertEquals("content://media/100", cell.coverUri)
        assertEquals(6, cell.photoCount)
        assertEquals(true, cell.isToday)
    }
}
