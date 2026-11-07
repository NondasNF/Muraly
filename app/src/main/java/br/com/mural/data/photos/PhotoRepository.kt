package br.com.mural.data.photos

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    private val mediaStorePhotoSource: MediaStorePhotoSource
) {
    private val monthCache = mutableMapOf<YearMonth, Map<LocalDate, DayPhotoSummary>>()

    suspend fun getMonthPhotoSummaries(month: YearMonth): Map<LocalDate, DayPhotoSummary> =
        withContext(Dispatchers.IO) {
            monthCache[month]?.let { return@withContext it }
            val summaries = mediaStorePhotoSource.getMonthPhotoSummaries(month)
            monthCache[month] = summaries
            summaries
        }

    suspend fun getDayPhotos(date: LocalDate): List<PhotoItem> =
        withContext(Dispatchers.IO) {
            mediaStorePhotoSource.getDayPhotos(date)
        }

    suspend fun getAllPhotos(): List<PhotoItem> =
        withContext(Dispatchers.IO) {
            mediaStorePhotoSource.getAllPhotos()
        }

    suspend fun validateMediaIds(mediaIds: Set<Long>): Set<Long> =
        withContext(Dispatchers.IO) {
            mediaStorePhotoSource.validateMediaIds(mediaIds)
        }

    suspend fun getOldestPhotoDate(): LocalDate? =
        withContext(Dispatchers.IO) {
            mediaStorePhotoSource.getOldestPhotoDate()
        }

    fun clearCache() {
        monthCache.clear()
    }
}
