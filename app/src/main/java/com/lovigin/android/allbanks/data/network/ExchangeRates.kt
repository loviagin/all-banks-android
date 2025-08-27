package com.lovigin.android.allbanks.data.network

data class ExchangeRates(
    val rates: Map<String, Rate>
) {
    data class Rate(val value: Double)
}

data class CoinGeckoRates(
    val rates: Map<String, CoinGeckoRate>
) {
    data class CoinGeckoRate(val value: Double)
}

data class ExchangeRateResponse(
    val rates: Map<String, Double>
)

data class FrankfurterResponse(
    val rates: Map<String, Double>,
    val base: String
)