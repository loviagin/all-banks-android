package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.UserEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.viewmodel.MainViewModel
import kotlinx.coroutines.launch

// используем наш enum с алиасом, чтобы избежать конфликта с java.util.Currency
import com.lovigin.android.allbanks.model.Currency as AppCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrenciesScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    val userFromVm by viewModel.currentUser.collectAsState()
    val userFromDb by db.userDao().observeFirst().collectAsState(initial = null)

    // текущее выбранное значение
    var selected by remember { mutableStateOf(AppCurrency.USD) }

    // инициализация как onAppear
    LaunchedEffect(userFromVm, userFromDb) {
        val code = (userFromVm?.defaultCurrency ?: userFromDb?.defaultCurrency) ?: "USD"
        selected = fromCodeSafe(code)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select main currency", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                Text(
                    "Main currency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            items(AppCurrency.values()) { c ->
                CurrencyRow(
                    currency = c,
                    selected = selected == c,
                    onSelect = { new ->
                        if (selected == new) return@CurrencyRow
                        selected = new
                        // сохранить в БД и синхронизировать с VM
                        scope.launch {
                            val current = userFromVm ?: userFromDb
                            val entity = if (current != null) {
                                // если userFromVm — это UserEntity, можно использовать прямо его
                                val base = if (userFromVm != null) userFromVm!! else current
                                base.copy(defaultCurrency = new.code)
                            } else {
                                UserEntity(name = "", email = "", defaultCurrency = new.code)
                            }
                            db.userDao().upsert(entity)
                            viewModel.setCurrentUser(entity)
                        }
                    }
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun CurrencyRow(
    currency: AppCurrency,
    selected: Boolean,
    onSelect: (AppCurrency) -> Unit
) {
    ListItem(
        headlineContent = { Text("${currency.displayName}") },
        supportingContent = { Text(currency.code) },
        trailingContent = {
            RadioButton(selected = selected, onClick = { onSelect(currency) })
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}

private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: AppCurrency.USD