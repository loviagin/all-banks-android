package com.lovigin.android.allbanks.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.data.local.entity.TransactionEntity
import com.lovigin.android.allbanks.model.Currency
import com.lovigin.android.allbanks.ui.components.BalanceCard
import com.lovigin.android.allbanks.ui.home.TransactionSheet
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.util.asDateString
import com.lovigin.android.allbanks.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())
    val accounts by db.accountDao().observeAll().collectAsState(initial = emptyList())
    val transactions by db.transactionDao().observeAll().collectAsState(initial = emptyList())

    val isLoading by viewModel.isLoading.collectAsState()
    val exchangeRates by viewModel.exchangeRates.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // локальные состояния
    var currentCurrency by remember { mutableStateOf(Currency.USD) }
    var balance by remember { mutableStateOf(0.0) }

    var showTx by remember { mutableStateOf(false) }
    var selectedTx by remember { mutableStateOf<TransactionEntity?>(null) }

    val darkTheme = isSystemInDarkTheme()
    val mainColor = if (darkTheme) MainDark else MainLight

    // аналог onAppear { setCurrency() }
    LaunchedEffect(currentUser?.defaultCurrency) {
        setCurrency(
            defaultCurrency = currentUser?.defaultCurrency,
            onSet = { curr, total ->
                currentCurrency = curr
                balance = total
            },
            calc = { curr -> viewModel.totalBalance(curr, accounts) }
        )
    }

    // аналог .onReceive(viewModel.$exchangeRates)
    LaunchedEffect(exchangeRates, accounts) {
        balance = viewModel.totalBalance(currentCurrency, accounts)
    }

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Home", color = mainColor) },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = Brand
//                )
//            )
//        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTx = null
                    showTx = true
                },
                containerColor = Brand,
                contentColor = mainColor
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    )  { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- BALANCES ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Your balances", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(
                        Modifier
                            .horizontalScroll(rememberScrollState())
                    ) {
                        BalanceCard(
                            currency = currentCurrency,
                            showBalance = !isLoading,
                            balance = balance,
                            text = "Total",
                            showMenu = false,
                            convert = { _, _ -> 0.0 }, // не используется
                        )

                        Spacer(Modifier.width(8.dp))

                        accounts.forEach { acc ->
                            val bankName = getBankName(acc.bankId, banks)
                            BalanceCard(
                                currency = Currency.fromCode(acc.currency)
                                    ?: Currency.USD, // TODO: строгая мапа
                                showBalance = true,
                                balance = acc.balance,
                                text = "$bankName · ${acc.name}",
                                showMenu = true,
                                convert = { fromCode, amount ->
                                    val from = Currency.fromCode(fromCode) ?: Currency.USD
                                    // используем VM.convert (совпадает с логикой Swift)
                                    Currency.entries // чтобы не терять совместимость
                                    viewModel.convert(from.code, amount, to = currentCurrency)
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                    }
                }

                // Правый столбец с двумя кнопками валют и меню
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Первая быстрая валюта
                    val quick1 = remember(currentCurrency) { getCurrency1(currentCurrency) }
                    TextButton(
                        onClick = {
                            currentCurrency = quick1
                            balance = viewModel.totalBalance(currentCurrency, accounts)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Text(quick1.symbol, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Вторая быстрая валюта
                    val quick2 = remember(currentCurrency) { getCurrency2(currentCurrency) }
                    TextButton(
                        onClick = {
                            currentCurrency = quick2
                            balance = viewModel.totalBalance(currentCurrency, accounts)
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    ) {
                        Text(quick2.symbol, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Меню выбора всех валют
                    CurrencyMenu(
                        current = currentCurrency,
                        onSelect = { c ->
                            currentCurrency = c
                            balance = viewModel.totalBalance(currentCurrency, accounts)
                        }
                    )
                }
            }

            // --- TRANSACTIONS header ---
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Brand
                )
                Spacer(Modifier.weight(1f))
                OutlinedButton(
                    onClick = { viewModel.update() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Brand
                    )
                ) {
                    Text("Refresh")
                }
            }

            // TipKit заглушки
            AssistiveTip(
                title = "Add banks and accounts",
                message = "On the relevant pages in the Account tab"
            )

            // Список транзакций
            if (transactions.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    // Аналог ContentUnavailableView
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                        Spacer(Modifier.height(8.dp))
                        Text("No transactions yet", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                AssistiveTip(
                    title = "View transaction details",
                    message = "Just tap on it"
                )

                val sortedTransactions = remember(transactions) {
                    transactions.sortedByDescending { it.dateMillis }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(sortedTransactions, key = { it.id }) { t ->
                        TransactionRow(
                            transaction = t,
                            bankName = getBankName(t.bankId, banks),
                            onClick = {
                                selectedTx = t
                                showTx = true
                            }
                        )
                    }
                }
            }
        }

        // Bottom sheet
        if (showTx) {
            ModalBottomSheet(
                onDismissRequest = { showTx = false; selectedTx = null }
            ) {
                TransactionSheet(
                    viewModel = viewModel,
                    initial = selectedTx,
                    onClose = { showTx = false; selectedTx = null },
                    onSaved = { showTx = false; selectedTx = null }
                )
            }
        }
    }
}

@Composable
private fun CurrencyMenu(
    current: Currency,
    onSelect: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilledTonalIconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.ExpandMore, contentDescription = "Choose currency")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.displayName) },
                    onClick = {
                        expanded = false
                        onSelect(currency)
                    }
                )
            }
        }
    }
}

@Composable
private fun AssistiveTip(title: String, message: String) {
    // Заглушка вместо TipKit
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionEntity,
    bankName: String,
    onClick: () -> Unit
) {
    val title = transaction.name ?: "Transaction in $bankName"
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(transaction.dateMillis.asDateString(), style = MaterialTheme.typography.labelMedium)
            }
            val sym = Currency.fromCode(transaction.currency)?.symbol ?: "$"
            Text(
                "$sym ${"%.2f".format(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionSheetContent(
    transaction: TransactionEntity?,
    onClose: () -> Unit
) {
    // TODO: полноценный экран редактирования/создания (порт твоего TransactionView)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            if (transaction == null) "New Transaction"
            else "Transaction Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Заглушка. Пришли SwiftUI TransactionView — перенесу 1:1."
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Text("Close")
        }
    }
}

private fun getBankName(id: java.util.UUID, banks: List<BankEntity>): String =
    banks.firstOrNull { it.id == id }?.name ?: ""

private fun getCurrency1(current: Currency): Currency =
    when (current) {
        Currency.USD -> Currency.EUR
        else -> Currency.USD
    }

private fun getCurrency2(current: Currency): Currency =
    when (current) {
        Currency.RUB -> Currency.GBP
        else -> Currency.RUB
    }

private fun setCurrency(
    defaultCurrency: String?,
    onSet: (Currency, Double) -> Unit,
    calc: (Currency) -> Double
) {
    if (!defaultCurrency.isNullOrEmpty()) {
        val curr = Currency.fromCode(defaultCurrency) ?: Currency.USD
        onSet(curr, calc(curr))
    }
}