package com.lovigin.android.allbanks.data.local

import androidx.room.TypeConverter
import java.util.*
import java.text.SimpleDateFormat
import java.util.Locale

class Converters {

    @TypeConverter fun fromUuid(u: UUID?): String? = u?.toString()
    @TypeConverter fun toUuid(s: String?): UUID? = s?.let(UUID::fromString)

    // Date
    @TypeConverter fun toDate(v: Long?): Date? = v?.let { Date(it) }

    // List<String>
    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    @TypeConverter fun fromDate(d: Date?): String? = d?.let { df.format(it) }
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(data: String): List<String> =
        if (data.isEmpty()) emptyList() else data.split(",")
    @TypeConverter fun fromStringList(list: List<String>?): String? =
        list?.joinToString("||")
    @TypeConverter fun toStringList(s: String?): List<String> =
        s?.takeIf { it.isNotEmpty() }?.split("||") ?: emptyList()

    // Список дат (для Loan.payments)
    @TypeConverter fun fromDateList(list: List<Date>?): String? =
        list?.joinToString("||") { df.format(it) }
    @TypeConverter fun toDateList(s: String?): List<Date> =
        s?.takeIf { it.isNotEmpty() }?.split("||")?.mapNotNull { df.parse(it) } ?: emptyList()
}