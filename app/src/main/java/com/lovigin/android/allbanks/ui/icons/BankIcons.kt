package com.lovigin.android.allbanks.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CurrencyFranc
import androidx.compose.material.icons.outlined.CurrencyLira
import androidx.compose.material.icons.outlined.CurrencyPound
import androidx.compose.material.icons.outlined.CurrencyRuble
import androidx.compose.material.icons.outlined.CurrencyYen
import androidx.compose.material.icons.outlined.CurrencyYuan
import androidx.compose.material.icons.outlined.EuroSymbol
import androidx.compose.ui.graphics.vector.ImageVector
import com.lovigin.android.allbanks.enums.Currency

/**
 * Возвращает иконку-символ валюты (или банковское здание, если для валюты нет спец. символа).
 * Использует material-icons-extended.
 *
 * Покрытие спец. символами:
 * - USD -> AttachMoney
 * - EUR -> EuroSymbol
 * - GBP -> CurrencyPound
 * - RUB -> CurrencyRuble
 * - CNY -> CurrencyYuan
 * - JPY -> CurrencyYen
 * - CHF -> CurrencyFranc
 * - TRY -> CurrencyLira
 *
 * Остальные (BYN, KZT, SEK, KRW, NOK, MXN) -> AccountBalance (универсальная «банковская» иконка).
 */

fun bankIconForCurrency(currency: Currency?): ImageVector {
    return when (currency) {
        Currency.USD -> Icons.Outlined.AttachMoney
        Currency.EUR -> Icons.Outlined.EuroSymbol
        Currency.GBP -> Icons.Outlined.CurrencyPound
        Currency.RUB -> Icons.Outlined.CurrencyRuble
        Currency.CNY -> Icons.Outlined.CurrencyYuan
        Currency.JPY -> Icons.Outlined.CurrencyYen
        Currency.CHF -> Icons.Outlined.CurrencyFranc
        Currency.TRY_ -> Icons.Outlined.CurrencyLira
        // нет отдельных иконок для этих валют в Material Icons:
        Currency.BYN, Currency.KZT, Currency.SEK, Currency.KRW, Currency.NOK, Currency.MXN,
        null -> Icons.Outlined.AccountBalance
    }
}

/** Удобная перегрузка для строкового кода валюты (напр. "USD"). */
fun bankIconForCurrencyCode(code: String?): ImageVector {
    val c = code?.let { Currency.fromCode(it) }
    return bankIconForCurrency(c)
}