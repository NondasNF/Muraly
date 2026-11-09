package br.com.mural.data.covers

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DayCoverEntity::class],
    version = 2,
    exportSchema = false
)
abstract class MuralDatabase : RoomDatabase() {
    abstract fun dayCoverDao(): DayCoverDao
}
