package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import kotlinx.coroutines.launch
import java.util.*

// используем наш enum с алиасом, чтобы избежать конфликта с java.util.Currency
import com.lovigin.android.allbanks.model.Currency as AppCurrency

@Composable
fun AccountSheet(
    initial: AccountEntity?,
    preselectedBank: BankEntity?,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())

    val isEditing = initial != null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bank by remember { mutableStateOf(preselectedBank ?: initial?.bankId?.let { id -> banks.firstOrNull { it.id == id } }) }
    var currency by remember { mutableStateOf(fromCodeSafe(initial?.currency ?: "USD")) }
    var balance by remember { mutableStateOf(initial?.balance ?: 0.0) }
    var number by remember { mutableStateOf(initial?.number ?: "") }

    var error by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(initial, banks) {
        if (bank == null && preselectedBank == null) {
            bank = initial?.bankId?.let { id -> banks.firstOrNull { it.id == id } }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            if (isEditing) initial!!.name else "New Account",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter account name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Банк
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

        // Валюта
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

        // Баланс
        OutlinedTextField(
            value = if (balance == 0.0) "" else balance.toString(),
            onValueChange = { v -> balance = v.toDoubleOrNull() ?: 0.0 },
            label = { Text("Current balance") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Номер счёта (опционально)
        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Account number (optional)") },
            modifier = Modifier.fillMaxWidth()
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
                    if (name.isBlank()) { error = "Name is required."; return@Button }
                    if (bank == null) { error = "Bank is required."; return@Button }

                    val entity = AccountEntity(
                        id = initial?.id ?: UUID.randomUUID(),
                        name = name,
                        number = number.ifBlank { null },
                        bankId = bank!!.id,
                        currency = currency.code,
                        balance = balance,
                        isCrypto = false,
                        isArchived = false,
                        isCredit = false
                    )

                    scope.launch {
                        db.accountDao().upsert(entity)
                        onSaved()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand)
            ) { Text("Save") }

            if (isEditing) {
                OutlinedButton(onClick = { showDeleteConfirm = true }) { Text("Delete") }
            }

            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClose) { Text("Close") }
        }
    }

    if (showDeleteConfirm && initial != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    // используем существующие db и scope, объявленные выше
                    scope.launch {
                        db.accountDao().delete(initial)
                        onClose()
                    }
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
            title = { Text("Delete account?") },
            text = { Text("Are you sure you want to delete this account?") }
        )
    }
}

private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AppCurrency.USD