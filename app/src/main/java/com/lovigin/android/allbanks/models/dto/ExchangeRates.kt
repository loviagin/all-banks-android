package com.lovigin.android.allbanks.models.dto

data class ExchangeRates(val rates: Map<String, Rate>) {
    data class Rate(val value: Double)
}