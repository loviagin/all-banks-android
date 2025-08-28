package com.lovigin.android.allbanks.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.model.Currency as AppCurrency

@Composable
fun AccountMiniView(
    account: AccountEntity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val symbol = fromCodeSafe(account.currency).symbol
    val title = account.name.ifBlank { account.number ?: "Account" }
    val icon = currencyIcon(account.currency)

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            Spacer(Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(symbol, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
                Text(String.format("%.2f", account.balance), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun fromCodeSafe(code: String): AppCurrency =
    AppCurrency.values().firstOrNull { it.code.equals(code, true) } ?: AppCurrency.USD

@Composable
private fun currencyIcon(code: String) = when (code.uppercase()) {
    "USD" -> Icons.Filled.AttachMoney
    "EUR" -> Icons.Filled.EuroSymbol
    "GBP" -> Icons.Filled.CurrencyPound
    "RUB" -> Icons.Filled.CurrencyRuble
    "CNY", "JPY" -> Icons.Filled.CurrencyYen
    else -> Icons.Filled.AccountBalance
}