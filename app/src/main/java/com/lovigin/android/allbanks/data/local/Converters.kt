package com.lovigin.android.allbanks.data.local

import androidx.room.TypeConverter
import java.util.UUID

class Converters {
    // UUID <-> String
    @TypeConverter
    fun fromUuid(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUuid(value: String?): UUID? = value?.let(UUID::fromString)

    // List<String> <-> String (|-separated)
    @TypeConverter
    fun fromStringList(list: List<String>?): String? = list?.joinToString("|")

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        value?.takeIf { it.isNotEmpty() }?.split("|") ?: emptyList()

    // List<Long> <-> String (, - separated)
    @TypeConverter
    fun fromLongList(list: List<Long>?): String? = list?.joinToString(",")

    @TypeConverter
    fun toLongList(value: String?): List<Long> =
        value?.takeIf { it.isNotEmpty() }?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()
}