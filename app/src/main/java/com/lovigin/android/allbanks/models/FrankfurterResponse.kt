package com.lovigin.android.allbanks.models

data class FrankfurterResponse(
    val rates: Map<String, Double>,
    val base: String
)