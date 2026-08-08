package com.rywent.langrid.di

import android.content.Context
import androidx.room.Room
import com.rywent.langrid.data.local.AppDatabase
import com.rywent.langrid.data.local.dao.DiaryDao
import com.rywent.langrid.data.local.dao.FolderDao
import com.rywent.langrid.data.local.dao.WordDao
import com.rywent.langrid.data.utils.WordImageStorage
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "langrid_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideWordDao(database: AppDatabase): WordDao {
        return database.wordDao()
    }

    @Provides
    fun provideDiaryDao(db: AppDatabase): DiaryDao = db.diaryDao()
    @Provides
    @Singleton
    fun provideWordImageStorage(
        @ApplicationContext context: Context
    ): WordImageStorage {
        return WordImageStorage(context)
    }
}