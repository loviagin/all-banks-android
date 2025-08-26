package com.lovigin.android.allbanks.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.models.Account
import com.lovigin.android.allbanks.enums.Currency
import com.lovigin.android.allbanks.viewmodels.MainViewModel

@Composable
fun HomeScreen(vm: MainViewModel) {
    val isLoading by vm.isLoading.collectAsState()
    val accounts: List<Account> = remember { emptyList() } // подключим DAO позже
    var currentCurrency by remember { mutableStateOf(Currency.USD) }
    var total by remember { mutableStateOf(0.0) }

    LaunchedEffect(vm.exchangeRates.collectAsState().value, accounts, currentCurrency) {
        total = vm.totalBalance(currentCurrency, accounts)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your balances", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                BalanceCard(
                    title = "Total",
                    currency = currentCurrency,
                    amount = total,
                    loading = isLoading
                )
            }
            items(accounts, key = { it.id }) { acc ->
                BalanceCard(
                    title = "${acc.name}",
                    currency = Currency.fromCode(acc.currency) ?: Currency.USD,
                    amount = acc.balance,
                    loading = false,
                    switchable = true,
                    onSwitch = { to ->
                        vm.convert(acc.currency, acc.balance, to)
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Быстрые кнопки смены валюты (как у тебя getCurrency1/getCurrency2)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { currentCurrency = if (currentCurrency == Currency.USD) Currency.EUR else Currency.USD }) {
                Text(if (currentCurrency == Currency.USD) "€" else "$")
            }
            Button(onClick = { currentCurrency = if (currentCurrency == Currency.RUB) Currency.GBP else Currency.RUB }) {
                Text(if (currentCurrency == Currency.RUB) "£" else "₽")
            }
        }
    }
}

@Composable
private fun BalanceCard(
    title: String,
    currency: Currency,
    amount: Double,
    loading: Boolean,
    switchable: Boolean = false,
    onSwitch: (Currency) -> Double = { 0.0 }
) {
    ElevatedCard {
        Column(Modifier.padding(12.dp)) {
            Row {
                Text(currency.symbol, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(8.dp))
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(String.format("%.2f", amount), style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}