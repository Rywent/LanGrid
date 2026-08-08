package com.rywent.langrid.di

import android.content.Context
import com.rywent.langrid.data.local.dao.DiaryDao
import com.rywent.langrid.data.local.dao.FolderDao
import com.rywent.langrid.data.local.dao.WordDao
import com.rywent.langrid.data.repository.DiaryRepository
import com.rywent.langrid.data.repository.FolderRepository
import com.rywent.langrid.data.repository.WordRepository
import com.rywent.langrid.data.utils.DiaryMediaStorage
import com.rywent.langrid.data.utils.VoiceNoteStorage
import com.rywent.langrid.data.utils.WordImageStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFolderRepository(
        folderDao: FolderDao,
        wordDao: WordDao
    ): FolderRepository {
        return FolderRepository(folderDao, wordDao)
    }

    @Provides
    @Singleton
    fun provideWordRepository(
        wordDao: WordDao,
        folderDao: FolderDao,
        imageStorage: WordImageStorage
    ): WordRepository {
        return WordRepository(wordDao, folderDao, imageStorage)
    }

    @Provides
    @Singleton
    fun provideVoiceNoteStorage(
        @ApplicationContext context: Context
    ): VoiceNoteStorage = VoiceNoteStorage(context)

    @Provides
    @Singleton
    fun provideDiaryMediaStorage(
        @ApplicationContext context: Context
    ): DiaryMediaStorage = DiaryMediaStorage(context)

    @Provides
    @Singleton
    fun provideDiaryRepository(
        diaryDao: DiaryDao,
        mediaStorage: DiaryMediaStorage
    ): DiaryRepository = DiaryRepository(diaryDao, mediaStorage)
}