package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String? = null,
    val amount: Double = 0.0,
    val account: UUID = UUID.randomUUID(),
    val bankId: UUID = UUID.randomUUID(),
    val currency: String = "USD",
    val conversationRate: Double? = null,
    val date: Date = Date(),
    val location: String? = null,
    val more: String? = null,
    val category: UUID? = null
)