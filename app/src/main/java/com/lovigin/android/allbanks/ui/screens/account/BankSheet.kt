package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DomainAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.ui.components.AccountMiniView
import com.lovigin.android.allbanks.ui.theme.Brand
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun BankSheet(
    initial: BankEntity?,
    onClose: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val allAccounts by db.accountDao().observeAll().collectAsState(initial = emptyList())
    val accounts = remember(initial, allAccounts) {
        initial?.let { b -> allAccounts.filter { it.bankId == b.id } } ?: emptyList()
    }

    var isEditing by remember { mutableStateOf(initial == null) }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var error by remember { mutableStateOf<String?>(null) }



    Column(
        Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        // Иконка и заголовок
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DomainAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                if (!isEditing && initial != null) initial.name else "Bank",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(12.dp))

        if (!isEditing && initial != null) {
            // режим просмотра
            Text("${accounts.size} accounts", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { isEditing = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("Edit bank")
                }
                OutlinedButton(onClick = { onDeleted() }) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp)); Text("Delete")
                }
            }

            if (accounts.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Accounts:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(accounts, key = { it.id }) { acc ->
                        var menuExpanded by remember { mutableStateOf(false) }

                        Box {
                            AccountMiniView(
                                account = acc,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                onClick = { menuExpanded = true }
                            )

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch {
                                            db.accountDao().delete(acc)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // режим редактирования/создания
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter bank name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (name.isBlank()) { error = "Name is required."; return@Button }
                    val entity = initial?.copy(name = name) ?: BankEntity(id = UUID.randomUUID(), name = name)
                    scope.launch {
                        db.bankDao().upsert(entity)
                        onSaved()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) { Text("Save") }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) { Text("Close") }
    }
}