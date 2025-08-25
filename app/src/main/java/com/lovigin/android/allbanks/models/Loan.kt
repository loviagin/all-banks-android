package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val bankId: UUID? = null,
    val amount: Double = 0.0,
    val currency: String = "USD",
    val isInstalments: Boolean = false,
    val duration: Int? = null, // days
    val payment: Double = 0.0,
    val payments: List<Date> = emptyList()
)