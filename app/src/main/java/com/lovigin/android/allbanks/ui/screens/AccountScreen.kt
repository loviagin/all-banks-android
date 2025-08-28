package com.lovigin.android.allbanks.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lovigin.android.allbanks.ui.screens.account.AccountsScreen
import com.lovigin.android.allbanks.ui.screens.account.BanksScreen
import com.lovigin.android.allbanks.ui.screens.account.CategoriesScreen
import com.lovigin.android.allbanks.ui.screens.account.CurrenciesScreen
import com.lovigin.android.allbanks.ui.screens.account.EditAccountScreen
import com.lovigin.android.allbanks.ui.screens.account.SettingsScreen
import com.lovigin.android.allbanks.ui.screens.account.SuggestCurrencyScreen
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.viewmodel.MainViewModel

sealed class AccountRoute(val route: String, val title: String) {
    data object Menu : AccountRoute("menu", "Account")
    data object EditAccount : AccountRoute("editAccount", "Edit Account")
    data object Banks : AccountRoute("banks", "My Banks")
    data object Accounts : AccountRoute("accounts", "My Accounts")
    data object Categories : AccountRoute("categories", "Categories")
    data object Settings : AccountRoute("settings", "Settings")
    data object Currencies : AccountRoute("currencies", "Currencies")
    data object SuggestCurrency : AccountRoute("suggestCurrency", "Suggest Currency")
}

@Composable
fun AccountScreen(viewModel: MainViewModel) {
    val nav = rememberNavController()
    AccountNavHost(nav = nav, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountNavHost(
    nav: NavHostController,
    viewModel: MainViewModel
) {
    Scaffold { padding ->
        NavHost(
            navController = nav,
            startDestination = AccountRoute.Menu.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AccountRoute.Menu.route) {
                AccountMenu(
                    viewModel = viewModel,
                    onEditAccount = { nav.navigate(AccountRoute.EditAccount.route) },
                    onBanks = { nav.navigate(AccountRoute.Banks.route) },
                    onAccounts = { nav.navigate(AccountRoute.Accounts.route) },
                    onCategories = { nav.navigate(AccountRoute.Categories.route) },
                    onSettings = { nav.navigate(AccountRoute.Settings.route) },
                    onCurrencies = { nav.navigate(AccountRoute.Currencies.route) },
                    onSuggestCurrency = { nav.navigate(AccountRoute.SuggestCurrency.route) }
                )
            }
            composable(AccountRoute.EditAccount.route) { EditAccountScreen(viewModel) }
            composable(AccountRoute.Banks.route) { BanksScreen() }
            composable(AccountRoute.Accounts.route) { AccountsScreen() }
            composable(AccountRoute.Categories.route) { CategoriesScreen() }
            composable(AccountRoute.Settings.route) { SettingsScreen() }
            composable(AccountRoute.Currencies.route) { CurrenciesScreen(viewModel) }
            composable(AccountRoute.SuggestCurrency.route) { SuggestCurrencyScreen(viewModel) }
        }
    }
}

@Composable
private fun AccountMenu(
    viewModel: MainViewModel,
    onEditAccount: () -> Unit,
    onBanks: () -> Unit,
    onAccounts: () -> Unit,
    onCategories: () -> Unit,
    onSettings: () -> Unit,
    onCurrencies: () -> Unit,
    onSuggestCurrency: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // HEADER: "Hi, {name}"
        item {
            Surface(
                onClick = onEditAccount,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Hi, ${user?.name?.ifBlank { "User" } ?: "User"}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // Section: General
        item {
            SectionHeader("General")
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.AccountBalance, contentDescription = null) },
                title = "My Banks",
                onClick = onBanks
            )
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.List, contentDescription = null) },
                title = "My Accounts",
                onClick = onAccounts
            )
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.Category, contentDescription = null) },
                title = "Categories",
                onClick = onCategories
            )
        }

        // Section: Settings
        item {
            SectionHeader("Settings")
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                title = "Settings",
                onClick = onSettings
            )
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.CurrencyExchange, contentDescription = null) },
                title = "Currencies",
                onClick = onCurrencies
            )
        }

        // Section: Suggest Currency
        item {
            SectionHeader("Other")
        }
        item {
            NavRow(
                icon = { Icon(Icons.Filled.Lightbulb, contentDescription = null) },
                title = "Suggest Currency",
                onClick = onSuggestCurrency,
                tonal = true
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
private fun NavRow(
    icon: @Composable () -> Unit,
    title: String,
    onClick: () -> Unit,
    tonal: Boolean = false
) {
    val bg = if (tonal) Brand.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
    Surface(
        color = bg,
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}