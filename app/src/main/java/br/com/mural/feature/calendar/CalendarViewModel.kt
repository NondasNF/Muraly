package br.com.mural.feature.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mural.core.date.DateUtils
import br.com.mural.core.permissions.PhotoAccess
import br.com.mural.core.permissions.PhotoPermissionHelper
import br.com.mural.data.covers.CoverRepository
import br.com.mural.data.covers.DayCoverEntity
import br.com.mural.data.photos.DayPhotoSummary
import br.com.mural.data.photos.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

sealed interface CalendarUiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : CalendarUiEvent
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coverRepository: CoverRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CalendarUiEvent>()
    val eventFlow: SharedFlow<CalendarUiEvent> = _eventFlow.asSharedFlow()

    init {
        observeCovers()
        loadInitialData()
    }

    fun updatePermissionState() {
        val accessState = PhotoPermissionHelper.getPhotoAccessState(context)
        _uiState.update { it.copy(access = accessState) }
        if (accessState is PhotoAccess.Granted || accessState is PhotoAccess.Partial) {
            loadMonthData(_uiState.value.month)
            loadTodayData()
        }
    }

    fun onMonthChanged(month: YearMonth) {
        _uiState.update { it.copy(month = month) }
        if (_uiState.value.access is PhotoAccess.Granted || _uiState.value.access is PhotoAccess.Partial) {
            loadMonthData(month)
        }
    }

    private fun observeCovers() {
        viewModelScope.launch {
            coverRepository.coversFlow.collectLatest { coversMap ->
                val currentMonth = _uiState.value.month
                updateCellsForMonth(currentMonth, coversMap)
                updateTodayState(coversMap)
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            updatePermissionState()
            val oldestDate = photoRepository.getOldestPhotoDate()
            val currentMonth = YearMonth.now()
            val minMonth = if (oldestDate != null) {
                YearMonth.from(oldestDate)
            } else {
                currentMonth.minusMonths(24)
            }
            _uiState.update {
                it.copy(
                    today = LocalDate.now(),
                    minMonth = minMonth,
                    maxMonth = currentMonth
                )
            }
        }
    }

    private fun loadMonthData(month: YearMonth) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val summaries = photoRepository.getMonthPhotoSummaries(month)
            val coversMap = coverRepository.coversFlow.value
            updateCellsForMonth(month, coversMap, summaries)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val summaries = photoRepository.getMonthPhotoSummaries(YearMonth.from(today))
            val todaySummary = summaries[today]
            val todayPhotoCount = todaySummary?.count ?: 0
            _uiState.update { it.copy(todayPhotoCount = todayPhotoCount) }
        }
    }

    private suspend fun updateCellsForMonth(
        month: YearMonth,
        coversMap: Map<String, DayCoverEntity>,
        summariesOverride: Map<LocalDate, DayPhotoSummary>? = null
    ) {
        val summaries = summariesOverride ?: photoRepository.getMonthPhotoSummaries(month)
        val rawCells = DateUtils.getMonthCells(month)
        val today = LocalDate.now()

        // Validate cover media IDs
        val coverMediaIds = coversMap.values.map { it.mediaId }.toSet()
        val validMediaIds = photoRepository.validateMediaIds(coverMediaIds)

        val cellUis = rawCells.map { date ->
            if (date == null) {
                // Ghost padding cell for 6-row alignment
                DayCellUi(
                    date = LocalDate.MIN,
                    coverUri = null,
                    photoCount = 0,
                    isToday = false,
                    isFuture = false,
                    isCoverAvailable = true
                )
            } else {
                val dateKey = DateUtils.toDateKey(date)
                val cover = coversMap[dateKey]
                val photoCount = summaries[date]?.count ?: 0
                val isToday = (date == today)
                val isFuture = date.isAfter(today)
                val isAvailable = cover == null || validMediaIds.contains(cover.mediaId)

                DayCellUi(
                    date = date,
                    coverUri = cover?.contentUri,
                    photoCount = photoCount,
                    isToday = isToday,
                    isFuture = isFuture,
                    isCoverAvailable = isAvailable
                )
            }
        }

        _uiState.update { it.copy(cells = cellUis) }
    }

    private fun updateTodayState(coversMap: Map<String, DayCoverEntity>) {
        val today = LocalDate.now()
        val dateKey = DateUtils.toDateKey(today)
        val cover = coversMap[dateKey]
        _uiState.update { it.copy(todayCoverUri = cover?.contentUri) }
    }

    fun undoCoverChange() {
        viewModelScope.launch {
            val success = coverRepository.undoLastChange()
            if (success) {
                _eventFlow.emit(CalendarUiEvent.ShowSnackbar("Ação desfeita com sucesso"))
            }
        }
    }
}
