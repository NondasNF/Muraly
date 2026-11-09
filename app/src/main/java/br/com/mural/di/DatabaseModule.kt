package br.com.mural.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.com.mural.data.covers.DayCoverDao
import br.com.mural.data.covers.MuralDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `day_covers_new` (
                `dateKey` TEXT NOT NULL,
                `mediaId` INTEGER,
                `contentUri` TEXT,
                `takenAtMillis` INTEGER,
                `noteText` TEXT NOT NULL DEFAULT '',
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`dateKey`)
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO `day_covers_new` (`dateKey`, `mediaId`, `contentUri`, `takenAtMillis`, `noteText`, `updatedAtMillis`)
            SELECT `dateKey`, `mediaId`, `contentUri`, `takenAtMillis`, '', `updatedAtMillis`
            FROM `day_covers`
        """.trimIndent())

        db.execSQL("DROP TABLE `day_covers`")

        db.execSQL("ALTER TABLE `day_covers_new` RENAME TO `day_covers`")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MuralDatabase {
        return Room.databaseBuilder(
            context,
            MuralDatabase::class.java,
            "mural.db"
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    fun provideDayCoverDao(database: MuralDatabase): DayCoverDao {
        return database.dayCoverDao()
    }
}
