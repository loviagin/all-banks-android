package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val email: String = "",
    val defaultCurrency: String = "USD",
    val favoritesCurrencies: List<String> = emptyList()
)