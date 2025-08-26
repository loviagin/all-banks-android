package com.lovigin.android.allbanks.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.models.Transaction
import java.util.*
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSheet(
    initial: Transaction?,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    onDelete: ((Transaction) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var amountText by remember { mutableStateOf((initial?.amount ?: 0.0).toString()) }
    var currency by remember { mutableStateOf(initial?.currency ?: "USD") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(if (initial == null) "New transaction" else "Edit transaction", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(name, { name = it }, label = { Text("Title (optional)") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(amountText, { amountText = it }, label = { Text("Amount") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(currency, { currency = it.uppercase() }, label = { Text("Currency") })
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val t = initial?.copy(
                            name = name.ifBlank { null },
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            currency = currency
                        ) ?: Transaction(
                            id = UUID.randomUUID(),
                            name = name.ifBlank { null },
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            account = UUID.randomUUID(),
                            bankId = UUID.randomUUID(),
                            currency = currency,
                            conversationRate = null,
                            date = Date(),
                            location = null,
                            more = null,
                            category = null
                        )
                        onSave(t)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }

                if (initial != null && onDelete != null) {
                    OutlinedButton(
                        onClick = { onDelete(initial) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) { Text("Delete") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}