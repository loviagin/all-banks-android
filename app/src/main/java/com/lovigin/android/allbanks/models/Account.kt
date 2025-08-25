package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val number: String? = null,
    val bankId: UUID = UUID.randomUUID(),
    val currency: String = "",
    val balance: Double = 0.0,
    val isCrypto: Boolean = false,
    val isArchived: Boolean = false,
    val isCredit: Boolean = false
)