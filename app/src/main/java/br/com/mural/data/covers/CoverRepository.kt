package br.com.mural.data.covers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

sealed interface UndoAction {
    data class RestoreCover(val cover: DayCoverEntity) : UndoAction
    data class RemoveCover(val dateKey: String) : UndoAction
}

@Singleton
class CoverRepository @Inject constructor(
    private val dao: DayCoverDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    val coversFlow: StateFlow<Map<String, DayCoverEntity>> = dao.observeAll()
        .map { list -> list.associateBy { it.dateKey } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyMap()
        )

    private var lastUndoAction: UndoAction? = null

    suspend fun getCoverForDate(dateKey: String): DayCoverEntity? {
        return dao.findByDate(dateKey)
    }

    suspend fun saveJournalNote(dateKey: String, noteText: String) {
        val existing = dao.findByDate(dateKey)
        val entity = DayCoverEntity(
            dateKey = dateKey,
            mediaId = existing?.mediaId,
            contentUri = existing?.contentUri,
            takenAtMillis = existing?.takenAtMillis,
            noteText = noteText,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsert(entity)
    }

    suspend fun setCover(
        dateKey: String,
        mediaId: Long,
        contentUri: String,
        takenAtMillis: Long
    ) {
        val previousCover = dao.findByDate(dateKey)
        lastUndoAction = if (previousCover != null) {
            UndoAction.RestoreCover(previousCover)
        } else {
            UndoAction.RemoveCover(dateKey)
        }

        val newCover = DayCoverEntity(
            dateKey = dateKey,
            mediaId = mediaId,
            contentUri = contentUri,
            takenAtMillis = takenAtMillis,
            noteText = previousCover?.noteText ?: "",
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsert(newCover)
    }

    suspend fun removeCover(dateKey: String) {
        val previousCover = dao.findByDate(dateKey)
        if (previousCover != null) {
            lastUndoAction = UndoAction.RestoreCover(previousCover)
            if (previousCover.noteText.isNotBlank()) {
                val entityWithoutCover = DayCoverEntity(
                    dateKey = dateKey,
                    mediaId = null,
                    contentUri = null,
                    takenAtMillis = null,
                    noteText = previousCover.noteText,
                    updatedAtMillis = System.currentTimeMillis()
                )
                dao.upsert(entityWithoutCover)
            } else {
                dao.delete(dateKey)
            }
        }
    }

    suspend fun undoLastChange(): Boolean {
        val action = lastUndoAction ?: return false
        lastUndoAction = null
        when (action) {
            is UndoAction.RestoreCover -> dao.upsert(action.cover)
            is UndoAction.RemoveCover -> dao.delete(action.dateKey)
        }
        return true
    }
}
