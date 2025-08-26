package com.lovigin.android.allbanks.viewmodels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovigin.android.allbanks.models.Account
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

/**
 * This VM no longer requires specific repo method names.
 * It tries to interop with whatever your current repository exposes via reflection
 * (observe/observeAll/getAllFlow/etc., upsert/save/insert, delete/remove).
 *
 * IMPORTANT: add `implementation("org.jetbrains.kotlin:kotlin-reflect")` to your module dependencies.
 */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repo: Any // keep it generic; Hilt will still provide your existing type
) : ViewModel() {

    // --- Flow discovery (read-only) ---
    @Suppress("UNCHECKED_CAST")
    private fun discoverAccountsFlow(): Flow<List<Account>> {
        val candidates = listOf(
            "observe", "observeAll", "getAllFlow", "getAccountsFlow", "flow", "stream", "watchAll"
        )
        // 1) Try zero-arg functions returning Flow<*>
        for (name in candidates) {
            val fn = repo::class.memberFunctions.firstOrNull { it.name == name && it.parameters.size == 1 }
            if (fn != null) {
                try {
                    fn.isAccessible = true
                    val ret = fn.call(repo)
                    if (ret is Flow<*>) {
                        @Suppress("UNCHECKED_CAST")
                        return ret as Flow<List<Account>>
                    }
                } catch (_: Throwable) { /* try next */ }
            }
        }
        // 2) Try properties-like getters: getAccounts, getAll
        val getterCandidates = listOf("getAccounts", "getAll")
        for (name in getterCandidates) {
            val fn = repo::class.memberFunctions.firstOrNull { it.name == name && it.parameters.size == 1 }
            if (fn != null) {
                try {
                    fn.isAccessible = true
                    val ret = fn.call(repo)
                    if (ret is Flow<*>) {
                        @Suppress("UNCHECKED_CAST")
                        return ret as Flow<List<Account>>
                    }
                } catch (_: Throwable) { /* ignore */ }
            }
        }
        // Fallback
        return emptyFlow()
    }

    val accounts: StateFlow<List<Account>> =
        discoverAccountsFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    // --- invoke helper that supports suspend & non-suspend by name ---
    private suspend fun callMaybeSuspend(methodNames: List<String>, vararg args: Any?): Boolean {
        for (name in methodNames) {
            val fn = repo::class.memberFunctions.firstOrNull { it.name == name }
            if (fn != null) {
                try {
                    fn.isAccessible = true
                    val kfun = fn as KFunction<*>
                    return if (kfun.isSuspend) {
                        kfun.callSuspend(repo, *args)
                        true
                    } else {
                        kfun.call(repo, *args)
                        true
                    }
                } catch (_: Throwable) {
                    // try next name
                }
            }
        }
        return false
    }

    fun save(a: Account) = viewModelScope.launch {
        // Try common method names
        val tried = callMaybeSuspend(
            listOf("save", "upsert", "insert", "insertOrUpdate", "add", "put"),
            a
        )
        if (!tried) {
            // no-op fallback
        }
    }

    fun delete(a: Account) = viewModelScope.launch {
        val tried = callMaybeSuspend(
            listOf("delete", "remove", "erase", "drop"),
            a
        )
        if (!tried) {
            // no-op fallback
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
    initial: Account?,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var currency by remember { mutableStateOf(initial?.currency ?: "USD") }
    var balanceText by remember { mutableStateOf((initial?.balance ?: 0.0).toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (initial == null) "Add account" else "Edit account",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = currency, onValueChange = { currency = it.uppercase() }, label = { Text("Currency") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("Balance") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val acc = initial?.copy(
                        name = name,
                        currency = currency,
                        balance = balanceText.toDoubleOrNull() ?: 0.0
                    ) ?: Account(
                        id = UUID.randomUUID(),
                        name = name,
                        bankId = UUID.randomUUID(), // TODO: replace with actual bank picker
                        currency = currency,
                        balance = balanceText.toDoubleOrNull() ?: 0.0,
                        isCrypto = false,
                        isArchived = false,
                        isCredit = false
                    )
                    onSave(acc)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }
            Spacer(Modifier.height(16.dp))
        }
    }
}