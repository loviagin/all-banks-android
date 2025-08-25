package com.lovigin.android.allbanks.models.dto

data class CoinGeckoRates(val rates: Map<String, CoinGeckoRate>) {
    data class CoinGeckoRate(val value: Double)
}