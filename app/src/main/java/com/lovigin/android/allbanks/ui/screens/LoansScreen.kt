package com.lovigin.android.allbanks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.LoanEntity
import com.lovigin.android.allbanks.model.Currency
import com.lovigin.android.allbanks.ui.components.LoanSheet
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    val loans by db.loanDao().observeAll().collectAsState(initial = emptyList())
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val exchangeRates by viewModel.exchangeRates.collectAsState()

    val dark = isSystemInDarkTheme()
    val mainColor = if (dark) MainDark else MainLight

    var selectedCurrency by remember { mutableStateOf(Currency.USD) }
    var loansBalance by remember { mutableStateOf(0.0) }  // sum of loan.payment
    var amountBalance by remember { mutableStateOf(0.0) } // sum of loan.amount
    var seeLoans by remember { mutableStateOf(false) }

    var showLoanDetails by remember { mutableStateOf(false) }
    var selectedLoan by remember { mutableStateOf<LoanEntity?>(null) }

    // на входе — ставим валюту пользователя и пересчитываем
    LaunchedEffect(currentUser?.defaultCurrency, exchangeRates, loans) {
        currentUser?.defaultCurrency?.takeIf { it.isNotEmpty() }?.let { code ->
            selectedCurrency = Currency.fromCode(code) ?: Currency.USD
        }
        loansBalance = calculateTotalPayment(selectedCurrency, loans, exchangeRates)
        amountBalance = calculateTotalAmount(selectedCurrency, loans, exchangeRates)
    }

    // при смене выбранной валюты
    LaunchedEffect(selectedCurrency) {
        loansBalance = calculateTotalPayment(selectedCurrency, loans, exchangeRates)
        amountBalance = calculateTotalAmount(selectedCurrency, loans, exchangeRates)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = selectedLoan == null)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedLoan = null
                    showLoanDetails = true
                },
                containerColor = Brand,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (loans.isEmpty()) {
                Box(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Brand.copy(alpha = 0.1f), shape = MaterialTheme.shapes.medium)
                ) {
                    Text(
                        "No loans",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Верхние карточки: total count + monthly payment (в выбранной валюте)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = Brand.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${loans.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Total loans")
                        }
                    }

                    // Меню валют + Monthly payment
                    Surface(
                        color = Brand.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.height(6.dp))
                            } else {
                                Text(
                                    "${selectedCurrency.symbol} ${"%.2f".format(loansBalance)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                            CurrencyDropdown(
                                current = selectedCurrency,
                                onSelect = { c -> selectedCurrency = c }
                            )
                            Text("Monthly payment")
                        }
                    }
                }

                // Total amount
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total amount:", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    Text(selectedCurrency.symbol)
                    Spacer(Modifier.width(6.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("${"%.2f".format(amountBalance)}")
                    }
                }

                // Календарь ближайших платежей
                if (seeLoans) {
                    val upcoming = remember(loans) { getUpcomingPayments(loans) }
                    Column(Modifier.padding(bottom = 10.dp)) {
                        Text("Upcoming payments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(8.dp))
                        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            upcoming.forEach { p ->
                                Surface(
                                    color = Brand.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLoan = loans.firstOrNull { it.id == p.loanId }
                                            showLoanDetails = true
                                        }
                                ) {
                                    Row(
                                        Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(p.loanName, fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(p.dayStr, fontWeight = FontWeight.Bold)
                                                Spacer(Modifier.width(6.dp))
                                                Text(p.monthStr, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                            }
                                        }
                                        Text(
                                            "${p.currency.symbol} ${"%.2f".format(p.amount)}",
                                            color = MainLight,
                                            modifier = Modifier
                                                .clip(MaterialTheme.shapes.small)
                                                .background(Brand)
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Toggle “See Loans & Calendar”
                TextButton(onClick = { seeLoans = !seeLoans }) {
                    Text(if (seeLoans) "Hide Loans & Calendar" else "See Loans & Calendar")
                }

                // Список кредитов
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(loans, key = { it.id }) { loan ->
                        LoanRow(
                            loan = loan,
                            onClick = {
                                selectedLoan = loan
                                showLoanDetails = true
                            }
                        )
                    }
                }
            }
        }

        // Bottom sheet: создание/редактирование кредита
        if (showLoanDetails) {
            ModalBottomSheet(
                onDismissRequest = {
                    showLoanDetails = false
                    selectedLoan = null
                },
                sheetState = sheetState
            ) {
                LoanSheet(
                    initial = selectedLoan,
                    onClose = {
                        showLoanDetails = false
                        selectedLoan = null
                    },
                    onSaved = {
                        showLoanDetails = false
                        selectedLoan = null
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrencyDropdown(
    current: Currency,
    onSelect: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Text(current.displayName)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Currency.entries.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.displayName) },
                    onClick = { expanded = false; onSelect(c) }
                )
            }
        }
    }
}

@Composable
private fun LoanRow(
    loan: LoanEntity,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        color = Brand.copy(alpha = 0.08f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(loan.name.ifBlank { "Loan" }, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text("Currency: ${loan.currency}")
            }
            val sym = Currency.fromCode(loan.currency)?.symbol ?: "$"
            Column(horizontalAlignment = Alignment.End) {
                Text("Total: $sym ${"%.2f".format(max(0.0, loan.amount))}", fontWeight = FontWeight.SemiBold)
                Text("Monthly: $sym ${"%.2f".format(max(0.0, loan.payment))}")
            }
        }
    }
}

private fun calculateTotalPayment(
    currency: Currency,
    loans: List<LoanEntity>,
    rates: Map<String, Double>
): Double = convertSum(loans.map { it.currency to it.payment }, currency.code, rates)

private fun calculateTotalAmount(
    currency: Currency,
    loans: List<LoanEntity>,
    rates: Map<String, Double>
): Double = convertSum(loans.map { it.currency to it.amount }, currency.code, rates)

private fun convertSum(
    items: List<Pair<String, Double>>,
    targetCode: String,
    rates: Map<String, Double>
): Double {
    val targetKey = targetCode.uppercase()
    val targetRate = rates[targetKey] ?: return 0.0
    if (rates.isEmpty()) return 0.0

    var total = 0.0
    items.forEach { (code, value) ->
        val key = code.uppercase()
        val rate = rates[key] ?: return@forEach
        val converted = when {
            key == targetKey -> value
            key == "USD" -> value * targetRate
            targetKey == "USD" -> value / rate
            else -> (value / rate) * targetRate
        }
        total += converted
    }
    return total
}

/** Мини-календарь: ближайшие платежи в след. месяц по первому платежу */
private data class PaymentDateVM(
    val date: Date,
    val amount: Double,
    val currency: Currency,
    val loanName: String,
    val loanId: UUID
) {
    val dayStr: String = java.text.SimpleDateFormat("d", java.util.Locale.getDefault()).format(date)
    val monthStr: String = java.text.SimpleDateFormat("MMM", java.util.Locale.getDefault()).format(date)
}

private fun getUpcomingPayments(loans: List<LoanEntity>): List<PaymentDateVM> {
    val cal = java.util.Calendar.getInstance()
    val today = cal.time

    val nextMonthCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MONTH, 1) }
    val nextMonth = nextMonthCal.time

    val list = mutableListOf<PaymentDateVM>()
    loans.forEach { loan ->
        val first = loan.payments.firstOrNull() ?: return@forEach
        var nextPayment = java.util.Date(first)

        // находим следующий платеж после сегодня (ежемесячно)
        while (!nextPayment.after(today)) {
            val c = java.util.Calendar.getInstance().apply { time = nextPayment; add(java.util.Calendar.MONTH, 1) }
            nextPayment = c.time
        }

        if (!nextPayment.after(nextMonth)) {
            val curr = Currency.fromCode(loan.currency) ?: Currency.USD
            list += PaymentDateVM(
                date = nextPayment,
                amount = loan.payment,
                currency = curr,
                loanName = loan.name.ifBlank { "Loan" },
                loanId = loan.id
            )
        }
    }
    return list.sortedBy { it.date }
}