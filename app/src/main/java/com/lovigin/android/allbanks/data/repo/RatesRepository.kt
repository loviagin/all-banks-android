package com.lovigin.android.allbanks.data.repo

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

class RatesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) {
    companion object {
        private const val TAG = "RatesRepository"
        private const val PREFS = "allbanks_prefs"
        private const val DONATION_ALLOWED = "donationAllowed"
    }

    suspend fun fetchExchangeRates(): Map<String, Double> {
        // 1) Frankfurter
        val frankfurterRates = runCatching { fetchFrankfurterUsdBase() }
            .onFailure { Log.e(TAG, "Frankfurter error", it) }
            .getOrDefault(emptyMap())

        // 2) CBR extra: RUB, BYN, KZT (в Swift ты расширяешь через ЦБ РФ)
        val cbr = runCatching { fetchCbrUsdExtras() }
            .onFailure { Log.e(TAG, "CBR error", it) }
            .getOrNull()

        val final = frankfurterRates.toMutableMap()
        cbr?.rub?.let { final["RUB"] = it }
        cbr?.byn?.let { final["BYN"] = it }
        cbr?.kzt?.let { final["KZT"] = it }

        // USD = 1.0 (на всякий случай)
        final["USD"] = 1.0

        return final
    }

    fun saveDonationAllowed(isAllowed: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(DONATION_ALLOWED, isAllowed)
            .apply()
    }

    fun getDonationAllowed(): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(DONATION_ALLOWED, false)

    suspend fun loadDonationAllowedFromApi(): Boolean {
        val req = Request.Builder()
            .url("https://lovigin.com/api/showDonation")
            .get()
            .build()
        val body = client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return false
            resp.body?.string().orEmpty()
        }
        val allowed = body.trim().lowercase(Locale.ROOT) == "true"
        saveDonationAllowed(allowed)
        Log.d(TAG, "Donation allowed: $allowed")
        return allowed
    }

    // ------ Helpers ------

    private fun fetchFrankfurterUsdBase(): Map<String, Double> {
        val req = Request.Builder()
            .url("https://api.frankfurter.app/latest?from=USD")
            .get()
            .build()
        val json = client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("Frankfurter HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        val root = JSONObject(json)
        val ratesObj = root.getJSONObject("rates")
        val result = mutableMapOf<String, Double>()
        for (key in ratesObj.keys()) {
            result[key.uppercase(Locale.ROOT)] = ratesObj.getDouble(key)
        }
        // USD не приходит — добавим при мерже
        return result
    }

    private data class CbrExtras(val rub: Double?, val byn: Double?, val kzt: Double?)

    /**
     * ЦБ РФ daily_json.js:
     * - RUB: берем USD.Value -> это RUB за 1 USD  => USD/RUB
     * - BYN, KZT: считаем через номинал и курс к RUB, потом через USD/RUB -> USD/BYN, USD/KZT
     */
    private fun fetchCbrUsdExtras(): CbrExtras {
        val req = Request.Builder()
            .url("https://www.cbr-xml-daily.ru/daily_json.js")
            .get()
            .build()
        val json = client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) error("CBR HTTP ${resp.code}")
            resp.body?.string().orEmpty()
        }
        val root = JSONObject(json)
        val valute = root.getJSONObject("Valute")

        var usdToRub: Double? = null
        val usd = valute.optJSONObject("USD")
        if (usd != null) {
            usdToRub = usd.optDouble("Value")
        }

        var bynUsd: Double? = null
        val byn = valute.optJSONObject("BYN") // руб за N BYN
        if (byn != null && usdToRub != null) {
            val bynValue = byn.optDouble("Value")
            val bynNom = byn.optDouble("Nominal")
            val bynToRub = bynValue / bynNom
            bynUsd = usdToRub / bynToRub // USD/BYN
        }

        var kztUsd: Double? = null
        val kzt = valute.optJSONObject("KZT")
        if (kzt != null && usdToRub != null) {
            val kztValue = kzt.optDouble("Value")
            val kztNom = kzt.optDouble("Nominal")
            val kztToRub = kztValue / kztNom
            kztUsd = usdToRub / kztToRub // USD/KZT
        }

        return CbrExtras(
            rub = usdToRub, // USD/RUB
            byn = bynUsd,   // USD/BYN
            kzt = kztUsd    // USD/KZT
        )
    }
}