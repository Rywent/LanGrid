package com.rywent.langrid.data.local.dao

import android.provider.SyncStateContract.Helpers.insert
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rywent.langrid.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words")
    fun observeAll(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE folderId = :folderId ORDER BY isPinned DESC, term ASC")
    fun observeByFolder(folderId: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    suspend fun getAll(): List<WordEntity>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: String): WordEntity?

    @Query("SELECT COUNT(*) FROM words WHERE folderId = :folderId")
    suspend fun countInFolder(folderId: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(word: WordEntity)
    @Update
    suspend fun update(word: WordEntity)

    suspend fun upsert(word: WordEntity) {
        if (getById(word.id) == null) insert(word)
        else update(word)
    }

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun delete(id: String)

    @Query("""
        SELECT * FROM words 
        WHERE term LIKE '%' || :q || '%' 
           OR translation LIKE '%' || :q || '%'
        LIMIT 50
    """)
    suspend fun search(q: String): List<WordEntity>
}