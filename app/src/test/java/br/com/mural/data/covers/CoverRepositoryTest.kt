package br.com.mural.data.covers

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CoverRepositoryTest {

    private class FakeDayCoverDao : DayCoverDao {
        private val storage = mutableMapOf<String, DayCoverEntity>()
        val flow = MutableStateFlow<List<DayCoverEntity>>(emptyList())

        override fun observeAll() = flow

        override suspend fun findByDate(dateKey: String): DayCoverEntity? = storage[dateKey]

        override suspend fun upsert(cover: DayCoverEntity) {
            storage[cover.dateKey] = cover
            flow.value = storage.values.toList()
        }

        override suspend fun delete(dateKey: String): Int {
            val removed = storage.remove(dateKey)
            flow.value = storage.values.toList()
            return if (removed != null) 1 else 0
        }
    }

    private lateinit var dao: FakeDayCoverDao
    private lateinit var repository: CoverRepository

    @Before
    fun setup() {
        dao = FakeDayCoverDao()
        repository = CoverRepository(dao)
    }

    @Test
    fun setCoverAddsCoverAndExposesItInCoversFlow() {
        runTest {
            repository.setCover("2026-11-12", 100L, "content://media/100", 10000L)

            val covers = repository.coversFlow.first()
            val cover = covers["2026-11-12"]

            assertEquals("2026-11-12", cover?.dateKey)
            assertEquals(100L, cover?.mediaId)
        }
    }

    @Test
    fun removeCoverDeletesCoverAndUndoRestoresIt() {
        runTest {
            repository.setCover("2026-11-12", 100L, "content://media/100", 10000L)
            repository.removeCover("2026-11-12")

            assertNull(repository.getCoverForDate("2026-11-12"))

            val undone = repository.undoLastChange()
            assertTrue(undone)
            assertEquals(100L, repository.getCoverForDate("2026-11-12")?.mediaId)
        }
    }
}
