package br.com.mural.data.photos

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import br.com.mural.core.date.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStorePhotoSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    fun getMonthPhotoSummaries(
        month: YearMonth,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Map<LocalDate, DayPhotoSummary> {
        val startMs = DateUtils.monthStartToEpochMillis(month, zoneId)
        val endMs = DateUtils.monthEndToEpochMillis(month, zoneId)
        val startSec = startMs / 1000
        val endSec = endMs / 1000

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = """
            ((${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} < ?)
            OR ((${MediaStore.Images.Media.DATE_TAKEN} IS NULL OR ${MediaStore.Images.Media.DATE_TAKEN} = 0)
                AND ${MediaStore.Images.Media.DATE_ADDED} >= ? AND ${MediaStore.Images.Media.DATE_ADDED} < ?))
        """.trimIndent()

        val selectionArgs = arrayOf(
            startMs.toString(),
            endMs.toString(),
            startSec.toString(),
            endSec.toString()
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        val summaries = mutableMapOf<LocalDate, MutableDaySummary>()

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)

                    val takenMs = if (dateTaken > 0) dateTaken else dateAdded * 1000
                    val date = DateUtils.epochMillisToLocalDate(takenMs, zoneId)

                    val current = summaries.getOrPut(date) {
                        MutableDaySummary(date = date, count = 0, latestMediaId = id)
                    }
                    current.count++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return summaries.mapValues { (_, value) ->
            DayPhotoSummary(
                date = value.date,
                count = value.count,
                latestMediaId = value.latestMediaId
            )
        }
    }

    fun getDayPhotos(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<PhotoItem> {
        val startMs = DateUtils.dayStartToEpochMillis(date, zoneId)
        val endMs = DateUtils.dayEndToEpochMillis(date, zoneId)
        val startSec = startMs / 1000
        val endSec = endMs / 1000

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val selection = """
            ((${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} < ?)
            OR ((${MediaStore.Images.Media.DATE_TAKEN} IS NULL OR ${MediaStore.Images.Media.DATE_TAKEN} = 0)
                AND ${MediaStore.Images.Media.DATE_ADDED} >= ? AND ${MediaStore.Images.Media.DATE_ADDED} < ?))
        """.trimIndent()

        val selectionArgs = arrayOf(
            startMs.toString(),
            endMs.toString(),
            startSec.toString(),
            endSec.toString()
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        return queryPhotos(projection, selection, selectionArgs, sortOrder)
    }

    fun getAllPhotos(): List<PhotoItem> {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC, ${MediaStore.Images.Media.DATE_ADDED} DESC"

        return queryPhotos(projection, null, null, sortOrder)
    }

    private fun queryPhotos(
        projection: Array<String>,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): List<PhotoItem> {
        val photos = mutableListOf<PhotoItem>()

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)

                    val takenMs = if (dateTaken > 0) dateTaken else dateAdded * 1000
                    val contentUri = ContentUris.withAppendedId(collectionUri, id).toString()

                    photos.add(
                        PhotoItem(
                            mediaId = id,
                            contentUri = contentUri,
                            takenAtMillis = takenMs,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return photos
    }

    fun validateMediaIds(mediaIds: Set<Long>): Set<Long> {
        if (mediaIds.isEmpty()) return emptySet()

        val validIds = mutableSetOf<Long>()
        val idPlaceholders = mediaIds.joinToString(",") { "?" }
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media._ID} IN ($idPlaceholders)"
        val selectionArgs = mediaIds.map { it.toString() }.toTypedArray()

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    validIds.add(cursor.getLong(idColumn))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return validIds
    }

    fun getOldestPhotoDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate? {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC, ${MediaStore.Images.Media.DATE_ADDED} ASC LIMIT 1"

        try {
            context.contentResolver.query(
                collectionUri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                    val dateTaken = cursor.getLong(dateTakenColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val takenMs = if (dateTaken > 0) dateTaken else dateAdded * 1000
                    if (takenMs > 0) {
                        return DateUtils.epochMillisToLocalDate(takenMs, zoneId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private data class MutableDaySummary(
        val date: LocalDate,
        var count: Int,
        val latestMediaId: Long
    )
}
