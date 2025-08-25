package com.lovigin.android.allbanks.models

data class ExchangeRates(val rates: Map<String, Rate>) {
    data class Rate(val value: Double)
}