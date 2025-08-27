package com.lovigin.android.allbanks.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lovigin.android.allbanks.ui.nav.SelectedTab
import com.lovigin.android.allbanks.ui.screens.AccountScreen
import com.lovigin.android.allbanks.ui.screens.HomeScreen
import com.lovigin.android.allbanks.ui.screens.LoansScreen
import com.lovigin.android.allbanks.viewmodel.MainViewModel

@Composable
fun ContentScreen(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == SelectedTab.Home,
                    onClick = { viewModel.select(SelectedTab.Home) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == SelectedTab.Statistics,
                    onClick = { viewModel.select(SelectedTab.Statistics) },
                    icon = { Icon(Icons.Filled.CreditCard, contentDescription = null) },
                    label = { Text("Loans") }
                )
                NavigationBarItem(
                    selected = selectedTab == SelectedTab.Account,
                    onClick = { viewModel.select(SelectedTab.Account) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Account") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            SelectedTab.Home -> HomeScreen()
            SelectedTab.Statistics -> LoansScreen()
            SelectedTab.Account -> AccountScreen()
        }
    }
}