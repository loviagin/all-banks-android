package com.lovigin.android.allbanks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val email: String = "",
    val defaultCurrency: String = "USD",
    val favoritesCurrencies: List<String> = emptyList()
)