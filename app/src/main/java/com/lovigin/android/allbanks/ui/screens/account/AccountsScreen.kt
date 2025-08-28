package com.lovigin.android.allbanks.ui.screens.account

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.model.Currency as AppCurrency
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())
    val accounts by db.accountDao().observeAll().collectAsState(initial = emptyList())

    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    var showSheet by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var selectedBank by remember { mutableStateOf<BankEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = selectedAccount == null && selectedBank != null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand),
                actions = {
                    TextButton(onClick = {
                        selectedAccount = null
                        selectedBank = null
                        showSheet = true
                    }) { Text("Add new", color = main) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedAccount = null
                    selectedBank = null
                    showSheet = true
                },
                containerColor = Brand,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            if (banks.isEmpty()) {
                // Пустое состояние: нет банков
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Brand.copy(alpha = 0.06f), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text("No banks yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("Go to “My Banks” and add a bank first.", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    banks.forEach { bank ->
                        val bankAccounts = accounts.filter { it.bankId == bank.id }   // ← обычный val

                        // Header
                        item(key = "header-${bank.id}") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(bank.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = {
                                    selectedBank = bank
                                    selectedAccount = null
                                    showSheet = true
                                }) { Text("+ Add", color = Brand) }
                            }
                        }

                        // Body
                        if (bankAccounts.isEmpty()) {
                            item(key = "empty-${bank.id}") {
                                Text(
                                    "No accounts",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(bankAccounts, key = { it.id }) { acc ->
                                AccountRow(
                                    account = acc,
                                    onClick = {
                                        selectedAccount = acc
                                        selectedBank = null
                                        showSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSheet = false
                        selectedAccount = null
                        selectedBank = null
                    },
                    sheetState = sheetState
                ) {
                    AccountSheet(
                        initial = selectedAccount,
                        preselectedBank = selectedBank,
                        onClose = {
                            showSheet = false
                            selectedAccount = null
                            selectedBank = null
                        },
                        onSaved = {
                            showSheet = false
                            selectedAccount = null
                            selectedBank = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountRow(
    account: AccountEntity,
    onClick: () -> Unit
) {
    val sym = fromCodeSafe(account.currency).symbol
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            Text("$sym ${"%.2f".format(account.balance)}")
        }
    }
}

// helper: безопасный поиск валюты по коду
private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AppCurrency.USD