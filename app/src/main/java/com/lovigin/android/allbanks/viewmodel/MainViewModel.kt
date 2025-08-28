package com.lovigin.android.allbanks.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.UserEntity
import com.lovigin.android.allbanks.data.repository.UserRepository
import com.lovigin.android.allbanks.model.Currency
import com.lovigin.android.allbanks.ui.nav.SelectedTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale

class MainViewModel(
    private val appContext: Context,
    private val repo: UserRepository
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences("allbanks_prefs", Context.MODE_PRIVATE)
    }
    private val http = OkHttpClient()

    // UI state
    private val _selectedTab = MutableStateFlow(SelectedTab.Home)
    val selectedTab: StateFlow<SelectedTab> = _selectedTab

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        update()
        getAllowanceFromApi()
    }

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
    }

    fun select(tab: SelectedTab) { _selectedTab.value = tab }

    fun update() {
        viewModelScope.launch {
            fetchExchangeRates { rates ->
                _exchangeRates.value = rates
            }
        }
        getAllowanceFromApi()
    }

    /** Аналог getUser() из Swift (в твоём ContentView он дергался на onAppear) */
    fun ensureUserLoaded() {
        viewModelScope.launch(Dispatchers.IO) {
            println("🔄 Загрузка пользователя...")
            val user = repo.getOrCreateUser()
            _currentUser.value = user
            println("✅ Пользователь загружен: ${user.name.ifBlank { "unknown" }}")
        }
    }

    // --- Donation allow flag ---
    private fun getAllowanceFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = Request.Builder()
                    .url("https://lovigin.com/api/showDonation")
                    .get()
                    .build()
                http.newCall(req).execute().use { resp ->
                    val body = resp.body?.string()?.trim()?.lowercase(Locale.US)
                    val isAllowed = (body == "true")
                    prefs.edit().putBoolean("donationAllowed", isAllowed).apply()
                    println("Donation allowed: $isAllowed")
                }
            } catch (e: Exception) {
                // тихо фейлим как в Swift-коде
            }
        }
    }

    // --- Конвертация валют ---
    fun convert(currency: String, balance: Double, to: Currency): Double {
        val targetKey = to.code.uppercase(Locale.US)
        val sourceKey = currency.uppercase(Locale.US)

        println("🎯 Конвертируем $balance $sourceKey -> $targetKey")

        val rates = _exchangeRates.value
        val targetRate = rates[targetKey]
        if (targetRate == null) {
            println("❌ Нет курса для целевой валюты $targetKey")
            return 0.0
        }

        val sourceRate = rates[sourceKey]
        if (sourceRate == null) {
            println("❌ Нет курса для исходной валюты $sourceKey")
            return 0.0
        }

        val converted = when {
            sourceKey == targetKey -> balance
            sourceKey == "USD"     -> balance * targetRate
            targetKey == "USD"     -> balance / sourceRate
            else -> {
                val usdValue = balance / sourceRate
                usdValue * targetRate
            }
        }

        println("💱 $balance $sourceKey -> $converted $targetKey")
        return converted
    }

    // --- Сумма по счетам в целевой валюте ---
    fun totalBalance(currentCurrency: Currency, accounts: List<AccountEntity>): Double {
        val targetKey = currentCurrency.code.uppercase(Locale.US)
        println("🎯 Рассчитываем баланс в $targetKey")

        val rates = _exchangeRates.value
        if (rates.isEmpty()) {
            println("⚠️ Курсы валют не загружены")
            return 0.0
        }

        println("📊 Доступные курсы:")
        rates.forEach { (k, v) -> println("   ${k.uppercase(Locale.US)}: $v") }

        val targetRate = rates[targetKey]
        if (targetRate == null) {
            println("❌ Нет курса для целевой валюты $targetKey")
            return 0.0
        }
        println("📊 Текущий курс $targetKey: $targetRate")

        return accounts
            .asSequence()
            .filter { !it.isArchived }
            .mapNotNull { acc ->
                val key = acc.currency.uppercase(Locale.US)
                println("📦 СЧЁТ: ${acc.name}, валюта $key, баланс ${acc.balance}")

                val rate = rates[key]
                if (rate == null) {
                    println("⚠️ Нет курса для ${acc.currency}")
                    null
                } else {
                    val converted = when {
                        key == targetKey -> acc.balance
                        key == "USD"     -> acc.balance * targetRate
                        targetKey == "USD" -> acc.balance / rate
                        else -> {
                            val usdValue = acc.balance / rate
                            usdValue * targetRate
                        }
                    }
                    println("💱 Конвертация: ${acc.balance} $key -> $converted $targetKey")
                    println("   Курс $key: $rate")
                    println("   Курс $targetKey: $targetRate")
                    converted
                }
            }
            .sum()
    }

    // --- Курсы валют ---
    fun fetchExchangeRates(completion: (Map<String, Double>) -> Unit) {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                println("🌍 Загружаем курсы...")

                // параллельные запросы как DispatchGroup в Swift
                val frankfurterDeferred = async { loadFrankfurterRates() }
                val cbrDeferred         = async { loadCbrRates() } // RUB, BYN, KZT через ЦБ РФ

                val frankfurterRates = frankfurterDeferred.await().toMutableMap()
                val (rubRate, bynRate, kztRate) = cbrDeferred.await()

                // USD базовая единица
                frankfurterRates["USD"] = 1.0
                if (rubRate != null) frankfurterRates["RUB"] = rubRate
                if (bynRate != null) frankfurterRates["BYN"] = bynRate
                if (kztRate != null) frankfurterRates["KZT"] = kztRate

                println("✅ Все курсы загружены:")
                frankfurterRates.forEach { (k, v) -> println("   $k: $v") }

                // Обновляем стейт на главном потоке
                launch(Dispatchers.Main) {
                    _exchangeRates.value = frankfurterRates
                    completion(frankfurterRates)
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    _isLoading.value = false
                }
            }
        }
    }

    // Frankfurter: https://api.frankfurter.app/latest?from=USD
    private fun loadFrankfurterRates(): Map<String, Double> {
        println("🌍 Загружаем курсы с Frankfurter API...")
        val req = Request.Builder()
            .url("https://api.frankfurter.app/latest?from=USD")
            .get()
            .build()
        http.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: return emptyMap()
            val json = JSONObject(body)
            val rates = json.getJSONObject("rates")

            val result = mutableMapOf<String, Double>()
            val keys = rates.keys()
            while (keys.hasNext()) {
                val key = keys.next().uppercase(Locale.US)
                val value = rates.optDouble(key, Double.NaN)
                if (!value.isNaN()) result[key] = value
            }
            println("✅ Курсы Frankfurter загружены (${result.size})")
            return result
        }
    }

    /**
     * ЦБ РФ: https://www.cbr-xml-daily.ru/daily_json.js
     * Возвращаем Triple(usdToRub, usdToByn, usdToKzt)
     *
     * Логика как в Swift:
     *  - RUB: берем USD.Value (RUB за 1 USD) → это сразу USD/RUB
     *  - BYN: ЦБ дает BYN/RUB (в рублях за номинал BYN). Сначала bynToRub = Value/Nominal,
     *         потом USD/BYN = (USD/RUB) / (BYN/RUB) = usdToRub / bynToRub
     *  - KZT: аналогично.
     */
    private fun loadCbrRates(): Triple<Double?, Double?, Double?> {
        println("🌍 Загружаем курсы с API ЦБ РФ...")
        val req = Request.Builder()
            .url("https://www.cbr-xml-daily.ru/daily_json.js")
            .get()
            .build()
        http.newCall(req).execute().use { resp ->
            val body = resp.body?.string() ?: return Triple(null, null, null)
            val root = JSONObject(body)
            val valute = root.optJSONObject("Valute") ?: return Triple(null, null, null)

            var usdToRub: Double? = null
            var usdToByn: Double? = null
            var usdToKzt: Double? = null

            // USD/RUB
            run {
                val usd = valute.optJSONObject("USD")
                val value = usd?.optDouble("Value")
                if (value != null && !value.isNaN()) {
                    usdToRub = value
                    println("✅ Курс RUB загружен: 1 USD = $usdToRub RUB")
                }
            }

            // USD/BYN
            run {
                val byn = valute.optJSONObject("BYN")
                val value = byn?.optDouble("Value")
                val nominal = byn?.optDouble("Nominal")
                if (value != null && nominal != null && nominal != 0.0 && usdToRub != null) {
                    val bynToRub = value / nominal // RUB за 1 BYN
                    usdToByn = usdToRub!! / bynToRub
                    println("✅ Курс BYN загружен: 1 USD = $usdToByn BYN")
                }
            }

            // USD/KZT
            run {
                val kzt = valute.optJSONObject("KZT")
                val value = kzt?.optDouble("Value")
                val nominal = kzt?.optDouble("Nominal")
                if (value != null && nominal != null && nominal != 0.0 && usdToRub != null) {
                    val kztToRub = value / nominal // RUB за 1 KZT
                    usdToKzt = usdToRub!! / kztToRub
                    println("✅ Курс KZT загружен: 1 USD = $usdToKzt KZT")
                }
            }

            return Triple(usdToRub, usdToByn, usdToKzt)
        }
    }

    companion object {
        fun factory(appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = AppDatabase.get(appContext)
                    val repo = UserRepository(db.userDao())
                    @Suppress("UNCHECKED_CAST")
                    return MainViewModel(appContext, repo) as T
                }
            }
    }
}