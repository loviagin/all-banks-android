package com.lovigin.android.allbanks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var name: String = "",
    var email: String = "",
    var defaultCurrency: String = "USD",
    var favoritesCurrencies: String = ""
)