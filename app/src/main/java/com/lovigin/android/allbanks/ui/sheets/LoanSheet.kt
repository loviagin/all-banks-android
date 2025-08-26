package com.lovigin.android.allbanks.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.models.Loan
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSheet(
    initial: Loan?,
    onDismiss: () -> Unit,
    onSave: (Loan) -> Unit,
    onDelete: ((Loan) -> Unit)? = null
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var currency by remember { mutableStateOf(initial?.currency ?: "USD") }
    var amountText by remember { mutableStateOf((initial?.amount ?: 0.0).toString()) }
    var paymentText by remember { mutableStateOf((initial?.payment ?: 0.0).toString()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(if (initial == null) "New loan" else "Edit loan", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(name, { name = it }, label = { Text("Name") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(currency, { currency = it.uppercase() }, label = { Text("Currency") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(amountText, { amountText = it }, label = { Text("Total amount") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(paymentText, { paymentText = it }, label = { Text("Monthly payment") })
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val l = initial?.copy(
                            name = name,
                            currency = currency,
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            payment = paymentText.toDoubleOrNull() ?: 0.0
                        ) ?: Loan(
                            id = UUID.randomUUID(),
                            name = name,
                            bankId = null,
                            amount = amountText.toDoubleOrNull() ?: 0.0,
                            currency = currency,
                            isInstalments = false,
                            duration = null,
                            payment = paymentText.toDoubleOrNull() ?: 0.0,
                            payments = listOf(Date())
                        )
                        onSave(l)
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }

                if (initial != null && onDelete != null) {
                    OutlinedButton(
                        onClick = { onDelete(initial) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Delete") }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}