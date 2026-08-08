package com.rywent.langrid.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rywent.langrid.data.local.dao.DiaryDao
import com.rywent.langrid.data.local.dao.FolderDao
import com.rywent.langrid.data.local.dao.WordDao
import com.rywent.langrid.data.local.entity.DiaryEntryEntity
import com.rywent.langrid.data.local.entity.FolderEntity
import com.rywent.langrid.data.local.entity.WordEntity

@Database(
    entities = [FolderEntity::class, WordEntity::class, DiaryEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun wordDao(): WordDao
    abstract fun diaryDao(): DiaryDao

    companion object {
        fun getDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "langrid_db")
                .fallbackToDestructiveMigration(false)
                .build()
    }
}