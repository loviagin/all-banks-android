package com.lovigin.android.allbanks.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.lovigin.android.allbanks.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun observeAll(): Flow<List<TransactionEntity>>
}