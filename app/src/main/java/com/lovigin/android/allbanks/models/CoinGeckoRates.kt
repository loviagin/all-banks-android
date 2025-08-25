package com.lovigin.android.allbanks.models

data class CoinGeckoRates(val rates: Map<String, CoinGeckoRate>) {
    data class CoinGeckoRate(val value: Double)
}
