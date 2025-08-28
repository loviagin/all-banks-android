package com.lovigin.android.allbanks.data.local.dao

import androidx.room.*
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface BankDao {
    @Query("SELECT * FROM banks ORDER BY name")
    fun observeAll(): Flow<List<BankEntity>>

    @Query("SELECT COUNT(*) FROM accounts WHERE bankId = :bankId")
    suspend fun accountsCount(bankId: UUID): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bank: BankEntity)

    @Delete
    suspend fun delete(bank: BankEntity)

    @Query("SELECT * FROM banks WHERE id = :id LIMIT 1")
    suspend fun getById(id: UUID): BankEntity?
}