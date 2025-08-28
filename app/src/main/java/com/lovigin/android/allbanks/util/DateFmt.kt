package com.lovigin.android.allbanks.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.asDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}