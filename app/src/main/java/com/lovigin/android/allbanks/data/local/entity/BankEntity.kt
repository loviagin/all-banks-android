package com.lovigin.android.allbanks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val isArchived: Boolean = false
)