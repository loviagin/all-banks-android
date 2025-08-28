package com.lovigin.android.allbanks.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import com.lovigin.android.allbanks.model.Currency
import com.lovigin.android.allbanks.ui.theme.Brand

@Composable
fun BalanceCard(
    currency: Currency,
    showBalance: Boolean,
    balance: Double,
    text: String,
    showMenu: Boolean,
    convert: (fromCurrencyCode: String, amount: Double) -> Double
) {
    var newCurrency by remember { mutableStateOf<Currency?>(null) }
    var newBalance by remember { mutableStateOf<Double?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val card = @Composable {
        Column(
            Modifier
                .widthIn(min = 220.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(Modifier)
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    (newCurrency ?: currency).symbol,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                if (!showBalance) {
                    // Аналог ProgressView
                    Text("…", style = MaterialTheme.typography.titleLarge)
                } else {
                    Text(
                        "%.2f".format(newBalance ?: balance),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }

    Surface(
        color = Brand.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = if (showMenu) Modifier.clickable { expanded = true } else Modifier
    ) {
        Box {
            card()
            if (showMenu) {
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(currency.displayName, color = Brand) },
                        onClick = {
                            newCurrency = null
                            newBalance = null
                            expanded = false
                        }
                    )
                    Currency.entries
                        .filter { it != currency }
                        .forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr.displayName, color = Brand) },
                                onClick = {
                                    newCurrency = curr
                                    newBalance = convert(currency.code, balance)
                                    expanded = false
                                }
                            )
                        }
                }
            }
        }
    }
}