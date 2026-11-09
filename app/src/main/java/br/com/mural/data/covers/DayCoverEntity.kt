package br.com.mural.data.covers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_covers")
data class DayCoverEntity(
    @PrimaryKey val dateKey: String,
    val mediaId: Long?,
    val contentUri: String?,
    val takenAtMillis: Long?,
    val noteText: String = "",
    val updatedAtMillis: Long
)
