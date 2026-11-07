package br.com.mural.feature.calendar

import androidx.compose.runtime.Immutable
import br.com.mural.core.permissions.PhotoAccess
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val cells: List<DayCellUi> = emptyList(),
    val access: PhotoAccess = PhotoAccess.Unknown,
    val isLoading: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    val todayCoverUri: String? = null,
    val todayPhotoCount: Int = 0,
    val minMonth: YearMonth = YearMonth.now().minusMonths(24),
    val maxMonth: YearMonth = YearMonth.now(),
    val cellAspectRatio: Float = 3f / 4f
)

@Immutable
data class DayCellUi(
    val date: LocalDate,
    val coverUri: String?,
    val photoCount: Int,
    val isToday: Boolean,
    val isFuture: Boolean,
    val isCoverAvailable: Boolean = true
)
