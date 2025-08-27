package com.lovigin.android.allbanks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "loans")
data class LoanEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val bankId: UUID? = null,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val isInstalments: Boolean = false,
    val durationDays: Int? = null,
    val payment: Double = 0.0,
    /** список дат платежей в epochMillis */
    val payments: List<Long> = emptyList()
)