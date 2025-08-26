package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.viewmodels.MainViewModel

@Composable
fun AccountScreen(vm: MainViewModel) {
    val user = vm.currentUser.collectAsState().value

    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text("Hi, ${user?.name ?: "User"}") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                }
            )
            Divider()

            Text("General", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))
            NavigationRow("My Banks")
            NavigationRow("My Accounts")
            NavigationRow("Categories")

            Text("Settings", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp))
            NavigationRow("Settings")
            NavigationRow("Currencies")
            NavigationRow("Suggest Currency")
        }
    }
}

@Composable
private fun NavigationRow(title: String) {
    ListItem(
        headlineContent = { Text(title) },
        modifier = Modifier.fillMaxWidth()
    )
    Divider()
}