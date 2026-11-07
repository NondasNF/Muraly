package br.com.mural.data.covers

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DayCoverDao {
    @Query("SELECT * FROM day_covers")
    fun observeAll(): Flow<List<DayCoverEntity>>

    @Query("SELECT * FROM day_covers WHERE dateKey = :dateKey")
    suspend fun findByDate(dateKey: String): DayCoverEntity?

    @Upsert
    suspend fun upsert(cover: DayCoverEntity)

    @Query("DELETE FROM day_covers WHERE dateKey = :dateKey")
    suspend fun delete(dateKey: String): Int
}
