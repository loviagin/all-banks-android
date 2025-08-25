package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "banks")
data class Bank(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val isArchived: Boolean = false
)