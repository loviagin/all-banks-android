package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BanksScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())
    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    var showSheet by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf<BankEntity?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = selectedBank == null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Banks", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand),
                actions = {
                    TextButton(onClick = {
                        selectedBank = null
                        showSheet = true
                    }) { Text("Add new", color = main) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedBank = null
                    showSheet = true
                },
                containerColor = Brand,
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (banks.isEmpty()) {
                // Пустое состояние + кнопка Add new
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Brand.copy(alpha = 0.06f), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("No banks yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            selectedBank = null
                            showSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = main)
                    ) { Text("Add new bank") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(banks, key = { it.id }) { bank ->
                        BankRow(
                            bank = bank,
                            accountsCountProvider = {
                                // Ленивая загрузка количества аккаунтов (корутина)
                                db.bankDao().accountsCount(bank.id)
                            },
                            onClick = {
                                selectedBank = bank
                                showSheet = true
                            }
                        )
                    }
                }
            }

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showSheet = false
                        selectedBank = null
                    },
                    sheetState = sheetState
                ) {
                    BankSheet(
                        initial = selectedBank,
                        onClose = {
                            showSheet = false
                            selectedBank = null
                        },
                        onDeleted = {
                            scope.launch {
                                selectedBank?.let { db.bankDao().delete(it) }
                                showSheet = false
                                selectedBank = null
                            }
                        },
                        onSaved = {
                            showSheet = false
                            selectedBank = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BankRow(
    bank: BankEntity,
    accountsCountProvider: suspend () -> Int,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableStateOf<Int?>(null) }

    // подгружаем количество аккуантов
    LaunchedEffect(bank.id) {
        count = accountsCountProvider()
    }

    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Domain, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(bank.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text("${count ?: 0} accounts", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}