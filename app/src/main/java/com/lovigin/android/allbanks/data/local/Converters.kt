package com.lovigin.android.allbanks.data.local

import androidx.room.TypeConverter
import java.util.*

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(millis: Long?): Date? = millis?.let { Date(it) }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? =
        list?.joinToString(separator = ",")

    @TypeConverter
    fun toStringList(data: String?): List<String> =
        data?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
}