package com.lovigin.android.allbanks.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovigin.android.allbanks.data.repo.RatesRepository
import com.lovigin.android.allbanks.data.repo.UserRepository
import com.lovigin.android.allbanks.models.Account
import com.lovigin.android.allbanks.enums.SelectedTab
import com.lovigin.android.allbanks.models.User
import com.lovigin.android.allbanks.enums.Currency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: RatesRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow<SelectedTab>(SelectedTab.HOME)
    val selectedTab: StateFlow<SelectedTab> = _selectedTab

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _exchangeRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val exchangeRates: StateFlow<Map<String, Double>> = _exchangeRates

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Подтянем/создадим пользователя сразу
        ensureUser()
        // И сразу подтянем курсы
        update()
        // Аналог getAllowanceFromApi()
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.loadDonationAllowedFromApi() }
        }
    }

    fun update() {
        fetchExchangeRates()
        // как в Swift — можно вызывать и тут
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.loadDonationAllowedFromApi() }
        }
    }

    private fun fetchExchangeRates() {
        if (_isLoading.value) return
        _isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val rates = runCatching { repo.fetchExchangeRates() }
                .getOrElse { emptyMap() }
            withContext(Dispatchers.Main) {
                _exchangeRates.value = rates
                _isLoading.value = false
            }
        }
    }

    /** Swift: convert(currency:balance:to:) */
    fun convert(currency: String, balance: Double, to: Currency): Double {
        val targetKey = to.code.uppercase(Locale.ROOT)
        val sourceKey = currency.uppercase(Locale.ROOT)

        val rates = _exchangeRates.value
        val targetRate = rates[targetKey] ?: return 0.0
        val sourceRate = rates[sourceKey] ?: return 0.0

        return when {
            sourceKey == targetKey -> balance
            sourceKey == "USD" -> balance * targetRate
            targetKey == "USD" -> balance / sourceRate
            else -> (balance / sourceRate) * targetRate
        }
    }

    /** Swift: totalBalance(currentCurrency:accounts:) */
    fun totalBalance(currentCurrency: Currency, accounts: List<Account>): Double {
        val targetKey = currentCurrency.code.uppercase(Locale.ROOT)
        val rates = _exchangeRates.value
        if (rates.isEmpty()) return 0.0
        val targetRate = rates[targetKey] ?: return 0.0

        return accounts
            .asSequence()
            .filter { !it.isArchived }
            .mapNotNull { acc ->
                val key = acc.currency.uppercase(Locale.ROOT)
                val rate = rates[key] ?: return@mapNotNull null
                when {
                    key == targetKey -> acc.balance
                    key == "USD" -> acc.balance * targetRate
                    targetKey == "USD" -> acc.balance / rate
                    else -> (acc.balance / rate) * targetRate
                }
            }
            .sum()
    }

    fun ensureUser() {
        viewModelScope.launch(Dispatchers.IO) {
            // Если пользователя ещё нет, создаём дефолтного в памяти
            val u = _currentUser.value ?: User(
                id = UUID.randomUUID(),
                name = "",
                email = "",
                defaultCurrency = "USD",
                favoritesCurrencies = emptyList()
            )
            withContext(Dispatchers.Main) { _currentUser.value = u }
        }
    }

    fun setSelectedTab(tab: SelectedTab) { _selectedTab.value = tab }
    fun setCurrentUser(user: User?) { _currentUser.value = user }
}