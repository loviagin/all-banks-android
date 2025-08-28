package com.lovigin.android.allbanks.ui.screens.account

import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.utils.GetHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    // собираем данные
    val banks = db.bankDao().observeAll().collectAsState(initial = emptyList()).value
    val accounts = db.accountDao().observeAll().collectAsState(initial = emptyList()).value
    val transactions = db.transactionDao().observeAll().collectAsState(initial = emptyList()).value
    val loans = db.loanDao().observeAll().collectAsState(initial = emptyList()).value
    val users = db.userDao().observeFirst().collectAsState(initial = null).value

    // donationAllowed из SharedPreferences (флаг, который ставит VM при запросе)
    var donationAllowed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val sp = context.getSharedPreferences("prefs", MODE_PRIVATE)
        donationAllowed = sp.getBoolean("donationAllowed", false)
    }

    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // summary card
            SummaryCard(
                banks = banks.size,
                accounts = accounts.size,
                transactions = transactions.size,
                loans = loans.size
            )

            // destructive
            Spacer(Modifier.height(8.dp))
            Surface(
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Delete all data",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(onClick = { showConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // developer info
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Developer info: LOVIGIN LTD")
                    Text("Contacts: support@lovigin.com")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Visit our website",
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clickable { uriHandler.openUri("https://lovigin.com") }
                    )

                    if (donationAllowed) {
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { uriHandler.openUri("https://lovigin.com/donation") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Brand,
                                contentColor = main
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text("Support our work")
                        }
                    }
                }
            }

            // version
            Spacer(Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Version: ${GetHelper.getAppVersion(context)}",
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Delete all data") },
            text = { Text("Are you sure you want to delete ALL local data? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        scope.launch {
                            // Полная очистка локальной БД
                            db.clearAllTables()
                        }
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SummaryCard(
    banks: Int,
    accounts: Int,
    transactions: Int,
    loans: Int
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Total banks: $banks")
            Text("Total accounts: $accounts")
            Text("Total transactions: $transactions")
            Text("Total loans: $loans")
        }
    }
}

private fun getAppVersion(context: android.content.Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = pInfo.versionName ?: "?.?.?"
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= 28) {
            pInfo.longVersionCode.toString()
        } else {
            @Suppress("DEPRECATION")
            pInfo.versionCode.toString()
        }
        "$versionName ($versionCode)"
    } catch (e: PackageManager.NameNotFoundException) {
        "unknown"
    }
}