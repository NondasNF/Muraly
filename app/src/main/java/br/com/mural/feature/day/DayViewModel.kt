package br.com.mural.feature.day

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.mural.core.date.DateUtils
import br.com.mural.data.covers.CoverRepository
import br.com.mural.data.photos.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface DayUiEvent {
    data object NavigateBackWithSuccess : DayUiEvent
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : DayUiEvent
}

@HiltViewModel
class DayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val coverRepository: CoverRepository,
    private val photoRepository: PhotoRepository
) : ViewModel() {

    val dateKey: String = checkNotNull(savedStateHandle["dateKey"])

    private val _uiState = MutableStateFlow(DayUiState(dateKey = dateKey))
    val uiState: StateFlow<DayUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<DayUiEvent>()
    val eventFlow: SharedFlow<DayUiEvent> = _eventFlow.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val date = DateUtils.parseDateKey(dateKey)
            val currentFilter = _uiState.value.filter
            val photos = if (currentFilter == PhotoFilter.SELECTED_DAY) {
                photoRepository.getDayPhotos(date)
            } else {
                photoRepository.getAllPhotos()
            }

            val savedRecord = coverRepository.getCoverForDate(dateKey)
            val initialNoteText = savedRecord?.noteText ?: ""

            _uiState.update {
                it.copy(
                    date = date,
                    photos = photos,
                    noteText = initialNoteText
                )
            }

            coverRepository.coversFlow.collectLatest { coversMap ->
                val currentCover = coversMap[dateKey]
                val selectedMediaId = currentCover?.mediaId ?: photos.firstOrNull()?.mediaId
                val isCoverAvailable = if (currentCover != null && currentCover.mediaId != null) {
                    val valid = photoRepository.validateMediaIds(setOf(currentCover.mediaId))
                    valid.contains(currentCover.mediaId)
                } else {
                    true
                }

                _uiState.update {
                    it.copy(
                        currentCover = currentCover,
                        selectedMediaId = _uiState.value.selectedMediaId ?: selectedMediaId,
                        noteText = currentCover?.noteText ?: _uiState.value.noteText,
                        isCoverAvailable = isCoverAvailable,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNoteTextChanged(newText: String) {
        _uiState.update { it.copy(noteText = newText) }
        viewModelScope.launch {
            coverRepository.saveJournalNote(dateKey, newText)
        }
    }

    fun setFilter(filter: PhotoFilter) {
        if (_uiState.value.filter == filter) return
        viewModelScope.launch {
            _uiState.update { it.copy(filter = filter, isLoading = true) }
            val date = _uiState.value.date
            val photos = if (filter == PhotoFilter.SELECTED_DAY) {
                photoRepository.getDayPhotos(date)
            } else {
                photoRepository.getAllPhotos()
            }
            _uiState.update {
                it.copy(
                    photos = photos,
                    isLoading = false,
                    selectedMediaId = it.currentCover?.mediaId ?: photos.firstOrNull()?.mediaId
                )
            }
        }
    }

    fun selectPhoto(mediaId: Long) {
        _uiState.update { it.copy(selectedMediaId = mediaId) }
    }

    fun openPhotoPicker() {
        _uiState.update { it.copy(isPhotoPickerOpen = true) }
    }

    fun closePhotoPicker() {
        _uiState.update { it.copy(isPhotoPickerOpen = false) }
    }

    fun openFullScreenViewer() {
        _uiState.update { it.copy(isFullScreenViewerOpen = true) }
    }

    fun closeFullScreenViewer() {
        _uiState.update { it.copy(isFullScreenViewerOpen = false) }
    }

    fun setCover() {
        val selectedId = _uiState.value.selectedMediaId ?: return
        val photo = _uiState.value.photos.find { it.mediaId == selectedId } ?: return

        viewModelScope.launch {
            coverRepository.setCover(
                dateKey = dateKey,
                mediaId = photo.mediaId,
                contentUri = photo.contentUri,
                takenAtMillis = photo.takenAtMillis
            )
            _uiState.update { it.copy(isPhotoPickerOpen = false) }
        }
    }

    fun removeCover() {
        viewModelScope.launch {
            coverRepository.removeCover(dateKey)
            _uiState.update { it.copy(isPhotoPickerOpen = false) }
        }
    }
}
