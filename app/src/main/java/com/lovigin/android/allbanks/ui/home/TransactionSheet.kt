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
import androidx.compose.ui.res.stringResource
import com.lovigin.android.allbanks.R
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
            if (isEditing) stringResource(R.string.transaction_str) else stringResource(R.string.new_transaction_str),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.enter_name_optional_str)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Банк
        var bankMenu by remember { mutableStateOf(false) }
        OutlinedButton(onClick = { bankMenu = true }, modifier = Modifier.fillMaxWidth()) {
            Text(bank?.name ?: stringResource(R.string.choose_bank_str))
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
            Text(account?.name ?: stringResource(R.string.choose_account_str))
        }
        DropdownMenu(expanded = accMenu, onDismissRequest = { accMenu = false }) {
            if (accounts.isEmpty()) {
                DropdownMenuItem(text = { Text(stringResource(R.string.no_accounts_available_str)) }, onClick = { })
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
        Text(stringResource(R.string.amount_str))
        OutlinedTextField(
            value = amountInput,
            onValueChange = { v -> amountInput = v.filter { it.isDigit() || it == '.' || it == '-' } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(stringResource(R.string.transaction_amount_str)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        // Курс, если валюта ввода != валюте счёта
        if (account != null && !currency.code.equals(account!!.currency, true)) {
            Text(stringResource(R.string.conversion_rate_str))
            OutlinedTextField(
                value = if (convRate == 0.0) "" else convRate.toString(),
                onValueChange = { v -> convRate = v.toDoubleOrNull() ?: 0.0 },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                placeholder = { Text(stringResource(R.string.conversion_rate_str)) },
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
            Text(category?.name ?: stringResource(R.string.choose_category_optional_str))
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
            label = { Text(stringResource(R.string.more_optional_str)) },
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
                    val b = bank ?: return@Button run { error =
                        context.getString(R.string.bank_is_required_str); }
                    val a = account ?: return@Button run { error =
                        context.getString(R.string.account_is_required_str); }
                    val rawAmount = amountInput.toDoubleOrNull() ?: return@Button run { error =
                        context.getString(
                            R.string.amount_cannot_be_zero_str
                        ); }
                    if (rawAmount == 0.0) { error = context.getString(R.string.amount_cannot_be_zero_str); return@Button }

                    // Если пользователь выбрал валюту, отличную от валюты счёта — курс обязателен
                    if (!currency.code.equals(a.currency, true) && convRate == 0.0) {
                        error = context.getString(R.string.conversion_rate_is_required_str)
                        return@Button
                    }

                    var final = rawAmount
                    if (!currency.code.equals(a.currency, true)) {
                        final *= convRate
                    }
                    // Правильная логика: доходы положительные, расходы отрицательные
                    if (txType == TxType.Expense) {
                        final = -Math.abs(final) // расходы всегда отрицательные
                    } else {
                        final = Math.abs(final)  // доходы всегда положительные
                    }

                    scope.launch {
                        if (initial == null) {
                            println("💰 Создание транзакции: сумма=$final, тип=$txType, счет=${a.name}")
                            println("💰 Баланс до обновления: ${a.balance}")
                            
                            // Создаём транзакцию и обновляем баланс атомарно
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
                            // обновляем баланс счёта
                            val newBalance = a.balance + final
                            println("💰 Новый баланс: $newBalance")
                            db.accountDao().upsert(a.copy(balance = newBalance))
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
            ) { Text(stringResource(R.string.save_str)) }

            if (initial != null) {
                var confirm by remember { mutableStateOf(false) }
                OutlinedButton(onClick = { confirm = true }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.delete_str))
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
                            }) { Text(stringResource(R.string.delete_str)) }
                        },
                        dismissButton = { TextButton(onClick = { confirm = false }) { Text(
                            stringResource(R.string.cancel_str)
                        ) } },
                        title = { Text(stringResource(R.string.delete_transaction_str)) },
                        text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_transaction_str)) }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text(stringResource(R.string.close_str)) }
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
            label = { Text(stringResource(R.string.income_str)) }
        )
        FilterChip(
            selected = selected == TxType.Expense,
            onClick = { onSelected(TxType.Expense) },
            label = { Text(stringResource(R.string.expense_str)) }
        )
    }
}

private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, true) } ?: AppCurrency.USD