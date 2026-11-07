package br.com.mural.di

import android.content.Context
import androidx.room.Room
import br.com.mural.data.covers.DayCoverDao
import br.com.mural.data.covers.MuralDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideDayCoverDao(database: MuralDatabase): DayCoverDao {
        return database.dayCoverDao()
    }
}
