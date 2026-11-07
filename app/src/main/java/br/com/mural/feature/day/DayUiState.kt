package br.com.mural.feature.day

import androidx.compose.runtime.Immutable
import br.com.mural.data.covers.DayCoverEntity
import br.com.mural.data.photos.PhotoItem
import java.time.LocalDate

enum class PhotoFilter {
    SELECTED_DAY,
    ALL_PHOTOS
}

@Immutable
data class DayUiState(
    val dateKey: String = "",
    val date: LocalDate = LocalDate.now(),
    val photos: List<PhotoItem> = emptyList(),
    val filter: PhotoFilter = PhotoFilter.SELECTED_DAY,
    val currentCover: DayCoverEntity? = null,
    val selectedMediaId: Long? = null,
    val isLoading: Boolean = true,
    val isCoverAvailable: Boolean = true
)
