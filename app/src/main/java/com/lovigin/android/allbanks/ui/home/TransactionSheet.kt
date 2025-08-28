package com.lovigin.android.allbanks.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.*
import com.lovigin.android.allbanks.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.*
import com.lovigin.android.allbanks.model.Currency as AppCurrency

enum class TxType { Income, Expense }

@Composable
fun TransactionSheet(
    viewModel: MainViewModel,
    initial: TransactionEntity?,
    onClose: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val banks by db.bankDao().observeAll().collectAsState(initial = emptyList())
    val accountsAll by db.accountDao().observeAll().collectAsState(initial = emptyList())
    val categories by db.categoryDao().observeAll().collectAsState(initial = emptyList())

    // state (порт @State)
    var isEditing by remember { mutableStateOf(initial != null) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var bank by remember { mutableStateOf<BankEntity?>(null) }
    var account by remember { mutableStateOf<AccountEntity?>(null) }
    var currency by remember { mutableStateOf(AppCurrency.USD) }          // вводимая валюта
    var amountInput by remember { mutableStateOf("") }                    // строкой, чтобы проще управлять
    var convRate by remember { mutableStateOf(0.0) }
    var dateMillis by remember { mutableStateOf(initial?.dateMillis ?: System.currentTimeMillis()) }
    var more by remember { mutableStateOf(initial?.more ?: "") }
    var category by remember { mutableStateOf<CategoryEntity?>(null) }
    var txType by remember { mutableStateOf(TxType.Expense) }
    var error by remember { mutableStateOf<String?>(null) }

    // init from initial
    LaunchedEffect(initial, banks, accountsAll, categories) {
        if (initial != null) {
            bank = banks.firstOrNull { it.id == initial.bankId }
            account = accountsAll.firstOrNull { it.id == initial.account }
            currency = fromCodeSafe(initial.currency) // фактически валюта счёта
            amountInput = "%.2f".format(initial.amount)
            convRate = initial.conversationRate ?: 0.0
            category = initial.category?.let { id -> categories.firstOrNull { it.id == id } }
            // тип — по знаку суммы
            txType = if ((initial.amount) >= 0) TxType.Income else TxType.Expense
        } else {
            // дефолты при создании
            // банк/счёт — не выставляем, ждём выбора
            val defaultCode = viewModel.currentUser.value?.defaultCurrency ?: "USD"
            currency = fromCodeSafe(defaultCode)
        }
    }

    // пересчёт курса при смене счёта/валюты
    fun updateConvRate() {
        val acc = account ?: return run { convRate = 0.0 }
        if (currency.code.equals(acc.currency, true)) {
            convRate = 0.0
            return
        }
        val srcKey = acc.currency.uppercase()
        val tgtKey = currency.code.uppercase()
        val rates = viewModel.exchangeRates.value
        val srcRate = rates[srcKey]
        val tgtRate = rates[tgtKey]
        convRate = when {
            srcRate == null || tgtRate == null -> 0.0
            srcKey == "USD" -> tgtRate
            tgtKey == "USD" -> srcRate
            else -> srcRate / tgtRate
        }
    }

    LaunchedEffect(account?.id, currency.code) { updateConvRate() }

    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            if (isEditing) "Transaction" else "New Transaction",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter name (optional)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Банк
        var bankMenu by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { bankMenu = true }, modifier = Modifier.fillMaxWidth()) {
            Text(bank?.name ?: "Choose bank")
        }
        DropdownMenu(expanded = bankMenu, onDismissRequest = { bankMenu = false }) {
            banks.forEach { b ->
                DropdownMenuItem(text = { Text(b.name) }, onClick = { bank = b; bankMenu = false; account = null })
            }
        }
        Spacer(Modifier.height(12.dp))

        // Счёт
        val accounts = remember(bank?.id, accountsAll) { accountsAll.filter { it.bankId == bank?.id } }
        var accMenu by remember { mutableStateOf(false) }
        OutlinedButton(
            onClick = { accMenu = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = bank != null
        ) {
            Text(account?.name ?: "Choose account")
        }
        DropdownMenu(expanded = accMenu, onDismissRequest = { accMenu = false }) {
            if (accounts.isEmpty()) {
                DropdownMenuItem(text = { Text("No accounts available") }, onClick = { })
            } else {
                accounts.forEach { a ->
                    DropdownMenuItem(text = { Text(a.name) }, onClick = { account = a; accMenu = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Валюта ввода
        var currMenu by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { currMenu = true }, modifier = Modifier.fillMaxWidth()) {
            Text("${currency.displayName} (${currency.code})")
        }
        DropdownMenu(expanded = currMenu, onDismissRequest = { currMenu = false }) {
            AppCurrency.values().forEach { c ->
                DropdownMenuItem(text = { Text(c.displayName) }, onClick = { currency = c; currMenu = false })
            }
        }
        Spacer(Modifier.height(12.dp))

        // Сумма
        Text("Amount:")
        OutlinedTextField(
            value = amountInput,
            onValueChange = { v -> amountInput = v.filter { it.isDigit() || it == '.' || it == '-' } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("Transaction amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Курс, если валюта ввода != валюте счёта
        if (account != null && !currency.code.equals(account!!.currency, true)) {
            Text("Conversion rate:")
            OutlinedTextField(
                value = if (convRate == 0.0) "" else convRate.toString(),
                onValueChange = { v -> convRate = v.toDoubleOrNull() ?: 0.0 },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text("Conversion rate") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }

        // Тип
        SegmentedButtons(txType) { txType = it }
        Spacer(Modifier.height(8.dp))

        // Дата (упростим: сейчас)
        // Можно добавить date/time picker, но оставим миллисекунды как есть
        // Spacer(Modifier.height(8.dp))

        // Категория
        var catMenu by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { catMenu = true }, modifier = Modifier.fillMaxWidth()) {
            Text(category?.name ?: "Choose category (optional)")
        }
        DropdownMenu(expanded = catMenu, onDismissRequest = { catMenu = false }) {
            categories.forEach { c ->
                DropdownMenuItem(text = { Text(c.name) }, onClick = { category = c; catMenu = false })
            }
        }
        Spacer(Modifier.height(12.dp))

        // Доп. поле
        OutlinedTextField(
            value = more,
            onValueChange = { more = it },
            label = { Text("More (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    // === save() ===
                    val b = bank ?: return@Button run { error = "Bank is required."; }
                    val a = account ?: return@Button run { error = "Account is required."; }
                    val rawAmount = amountInput.toDoubleOrNull() ?: return@Button run { error = "Amount cannot be zero."; }
                    if (rawAmount == 0.0) { error = "Amount cannot be zero."; return@Button }

                    // Если пользователь выбрал валюту, отличную от валюты счёта — курс обязателен
                    if (!currency.code.equals(a.currency, true) && convRate == 0.0) {
                        error = "Conversion rate is required."
                        return@Button
                    }

                    var final = rawAmount
                    if (!currency.code.equals(a.currency, true)) {
                        final *= convRate
                    }
                    if (txType == TxType.Expense && final > 0) final *= -1

                    scope.launch {
                        if (initial == null) {
                            // создаём транзакцию и обновляем баланс счёта
                            val entity = TransactionEntity(
                                id = UUID.randomUUID(),
                                name = name.ifBlank { null },
                                amount = final,
                                account = a.id,
                                bankId = b.id,
                                currency = a.currency,                 // храним валюту счёта
                                conversationRate = if (convRate == 0.0) null else convRate,
                                dateMillis = dateMillis,
                                more = more.ifBlank { null },
                                category = category?.id
                            )
                            db.transactionDao().upsert(entity)
                            // обновим баланс
                            db.accountDao().upsert(a.copy(balance = a.balance + final))
                        } else {
                            // как в Swift — редактируем только name (и при желании more)
                            val updated = initial.copy(
                                name = name.ifBlank { null },
                                more = more.ifBlank { null }
                            )
                            db.transactionDao().upsert(updated)
                        }
                        onSaved()
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Save") }

            if (initial != null) {
                var confirm by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { confirm = true }, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
                if (confirm) {
                    AlertDialog(
                        onDismissRequest = { confirm = false },
                        confirmButton = {
                            TextButton(onClick = {
                                confirm = false
                                scope.launch {
                                    // откатываем баланс счёта и удаляем транзакцию
                                    val acc = accountsAll.firstOrNull { it.id == initial.account }
                                    if (acc != null) {
                                        db.accountDao().upsert(acc.copy(balance = acc.balance - initial.amount))
                                    }
                                    db.transactionDao().delete(initial)
                                    onClose()
                                }
                            }) { Text("Delete") }
                        },
                        dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } },
                        title = { Text("Delete transaction?") },
                        text = { Text("Are you sure you want to delete this transaction?") }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Close") }
    }
}

@Composable
private fun SegmentedButtons(
    selected: TxType,
    onSelected: (TxType) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selected == TxType.Income,
            onClick = { onSelected(TxType.Income) },
            label = { Text("Income") }
        )
        FilterChip(
            selected = selected == TxType.Expense,
            onClick = { onSelected(TxType.Expense) },
            label = { Text("Expense") }
        )
    }
}

private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, true) } ?: AppCurrency.USD