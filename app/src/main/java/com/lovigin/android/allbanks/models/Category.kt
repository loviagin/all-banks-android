package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val icon: String = ""
)