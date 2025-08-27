package com.lovigin.android.allbanks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String? = null,
    val amount: Double = 0.0,
    val account: UUID = UUID.randomUUID(),
    val bankId: UUID = UUID.randomUUID(),
    val currency: String = "USD",
    val conversationRate: Double? = null,
    /** дата операции в epochMillis */
    val dateMillis: Long = System.currentTimeMillis(),
    val location: String? = null,
    val more: String? = null,
    val category: UUID? = null
)