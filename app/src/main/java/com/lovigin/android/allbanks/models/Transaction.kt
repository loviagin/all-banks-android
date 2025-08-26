package com.lovigin.android.allbanks.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.*

@TypeConverters(UuidDateConverters::class)
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

class UuidDateConverters {
    @TypeConverter fun fromUuid(value: UUID?): String? = value?.toString()
    @TypeConverter fun toUuid(value: String?): UUID? = value?.let { UUID.fromString(it) }
    @TypeConverter fun fromDate(value: Date?): Long? = value?.time
    @TypeConverter fun toDate(value: Long?): Date? = value?.let { Date(it) }
}