package com.lovigin.android.allbanks.ui.screens.loans

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.enums.Currency
import com.lovigin.android.allbanks.models.Loan
import com.lovigin.android.allbanks.viewmodels.MainViewModel

@Composable
fun LoansScreen(vm: MainViewModel) {
    val loans: List<Loan> = remember { emptyList() } // подключим DAO позже
    var selectedCurrency by remember { mutableStateOf(Currency.USD) }
    var monthly by remember { mutableDoubleStateOf(0.0) }
    var totalAmount by remember { mutableDoubleStateOf(0.0) }

    fun recalc() {
        monthly = loans.sumOf { loan ->
            vm.convert(loan.currency, loan.payment, selectedCurrency)
        }
        totalAmount = loans.sumOf { loan ->
            vm.convert(loan.currency, loan.amount, selectedCurrency)
        }
    }

    LaunchedEffect(vm.exchangeRates.collectAsState().value, loans, selectedCurrency) { recalc() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("My loans", style = MaterialTheme.typography.titleLarge)
            Button(onClick = { /* open bottom sheet Add/Edit Loan */ }) {
                Text("Add")
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            StatCard("Total loans", "${loans.size}")
            StatCard("Monthly payment", "${selectedCurrency.symbol} ${"%.2f".format(monthly)}")
        }

        Spacer(Modifier.height(12.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total amount:", style = MaterialTheme.typography.bodyLarge)
            Text("${selectedCurrency.symbol} ${"%.2f".format(totalAmount)}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    ElevatedCard(Modifier.widthIn(min = 140.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}