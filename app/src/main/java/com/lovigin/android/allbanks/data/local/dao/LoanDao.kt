package com.lovigin.android.allbanks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lovigin.android.allbanks.data.local.entity.LoanEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans")
    fun observeAll(): Flow<List<LoanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LoanEntity)

    @Delete
    suspend fun delete(entity: LoanEntity)

    @Query("SELECT * FROM loans WHERE id = :id LIMIT 1")
    suspend fun getById(id: UUID): LoanEntity?
}