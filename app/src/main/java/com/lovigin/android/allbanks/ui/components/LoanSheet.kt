package com.lovigin.android.allbanks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.data.local.entity.LoanEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ВАЖНО: алиас, чтобы не конфликтовать с java.util.Currency и др.
import com.lovigin.android.allbanks.model.Currency as AppCurrency

@Composable
fun LoanSheet(
    initial: LoanEntity?,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())

    // --- Состояния ---
    val isEditing = initial != null
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var bank by remember { mutableStateOf<BankEntity?>(null) }

    // Валюта: берём из initial или USD
    var currency by remember {
        mutableStateOf(
            initial?.currency?.let { fromCodeSafe(it) } ?: AppCurrency.USD
        )
    }

    var amount by remember { mutableStateOf(initial?.amount ?: 0.0) }
    var payment by remember { mutableStateOf(initial?.payment ?: 0.0) }
    var paymentDateMillis by remember { mutableStateOf(initial?.payments?.firstOrNull() ?: System.currentTimeMillis()) }

    var error by remember { mutableStateOf<String?>(null) }

    // Найти bank из initial
    LaunchedEffect(initial, banks) {
        bank = initial?.bankId?.let { id -> banks.firstOrNull { it.id == id } }
    }

    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            if (isEditing) name.ifBlank { "Loan" } else "New Loan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        // Название
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter loan name (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Банк (простое меню)
        var bankMenu by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = { bankMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(bank?.name ?: "Choose bank")
        }
        DropdownMenu(expanded = bankMenu, onDismissRequest = { bankMenu = false }) {
            banks.forEach { b ->
                DropdownMenuItem(
                    text = { Text(b.name) },
                    onClick = { bank = b; bankMenu = false }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Валюта (простое меню)
        var currencyMenu by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = { currencyMenu = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("${currency.displayName} (${currency.code})")
        }
        DropdownMenu(expanded = currencyMenu, onDismissRequest = { currencyMenu = false }) {
            AppCurrency.values().forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.displayName) },
                    onClick = { currency = c; currencyMenu = false }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // Total amount
        OutlinedTextField(
            value = if (amount == 0.0) "" else amount.toString(),
            onValueChange = { v -> amount = v.toDoubleOrNull() ?: 0.0 },
            label = { Text("Total amount") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Monthly payment
        OutlinedTextField(
            value = if (payment == 0.0) "" else payment.toString(),
            onValueChange = { v -> payment = v.toDoubleOrNull() ?: 0.0 },
            label = { Text("Monthly payment") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Дата старта (пока просто текст; можно прикрутить DatePicker)
        Text(
            "Start date: ${sdf.format(Date(paymentDateMillis))}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    // Валидация
                    if (bank == null) { error = "Bank is required."; return@Button }
                    if (amount <= 0.0) { error = "Amount must be greater than zero."; return@Button }
                    if (payment <= 0.0) { error = "Monthly payment must be greater than zero."; return@Button }

                    val finalName = if (name.isBlank()) "Loan at ${bank!!.name}" else name

                    val entity = LoanEntity(
                        id = initial?.id ?: UUID.randomUUID(),
                        name = finalName,
                        bankId = bank!!.id,
                        amount = amount,
                        currency = currency.code, // теперь точно String
                        isInstalments = false,
                        durationDays = null,
                        payment = payment,
                        payments = listOf(paymentDateMillis)
                    )

                    scope.launch {
                        db.loanDao().upsert(entity)
                        onSaved()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) {
                Text("Save")
            }

            if (isEditing && initial != null) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            db.loanDao().delete(initial)
                            onClose()
                        }
                    }
                ) { Text("Delete") }
            }

            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Close") }
        }
    }
}

/** Безопасная версия fromCode: не требует entries, работает на values() */
private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AppCurrency.USD