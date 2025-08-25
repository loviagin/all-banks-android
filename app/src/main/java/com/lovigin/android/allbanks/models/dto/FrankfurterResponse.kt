package com.lovigin.android.allbanks.models.dto

data class FrankfurterResponse(
    val rates: Map<String, Double>,
    val base: String
)