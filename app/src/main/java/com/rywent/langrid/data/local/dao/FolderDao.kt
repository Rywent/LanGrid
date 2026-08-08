package com.rywent.langrid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rywent.langrid.data.local.entity.FolderEntity
import com.rywent.langrid.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {

    @Query("SELECT * FROM folders")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY isPinned DESC, updatedAt DESC")
    fun observeRootFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE parentId = :parentId ORDER BY isPinned DESC, title ASC")
    fun observeChildren(parentId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    suspend fun upsert(folder: FolderEntity) {
        if (getById(folder.id) == null) insert(folder)
        else update(folder)
    }

    @Query("UPDATE folders SET updatedAt = :ts WHERE id = :id")
    suspend fun touch(id: String, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun delete(id: String)
}

