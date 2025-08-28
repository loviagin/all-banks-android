package com.lovigin.android.allbanks.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.ui.graphics.vector.ImageVector
import com.lovigin.android.allbanks.data.local.entity.AccountEntity
import com.lovigin.android.allbanks.data.local.entity.BankEntity
import com.lovigin.android.allbanks.model.Currency as AppCurrency

object GetHelper {

    /** Аналог Swift: безопасно парсим код валюты в enum, иначе USD */
    fun getCurrency(code: String): AppCurrency {
        val found = AppCurrency.values().firstOrNull { it.code.equals(code, ignoreCase = true) }
        if (found == null) {
            android.util.Log.w("GetHelper", "Unknown currency code: $code → fallback USD")
        }
        return found ?: AppCurrency.USD
    }

    /** Сколько счетов у банка */
    fun getAccountsCount(forBank: BankEntity, accounts: List<AccountEntity>): Int =
        accounts.count { it.bankId == forBank.id }

    /** Иконка банка по коду валюты (подбор ближайшего Material Icons аналога) */
    fun getBankIcon(code: String?): ImageVector {
        val c = code?.uppercase().orEmpty()
        return when (c) {
            "USD" -> Icons.Filled.AttachMoney
            "EUR" -> Icons.Filled.EuroSymbol
            "GBP" -> Icons.Filled.CurrencyPound
            "RUB" -> Icons.Filled.CurrencyRuble
            "JPY", "CNY" -> Icons.Filled.CurrencyYen
            // нет прямых иконок CHF/SEK/KRW/NOK/MXN/TRY в стандартном наборе — fallback:
            "CHF", "SEK", "KRW", "NOK", "MXN", "TRY" -> Icons.Filled.AccountBalance
            else -> Icons.Filled.AccountBalance
        }
    }

    /** Версия приложения: "Version x.y.z (build)" */
    fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = pInfo.versionName ?: "Unknown version"
            val build = if (android.os.Build.VERSION.SDK_INT >= 28) {
                pInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode.toString()
            }
            "Version $versionName ($build)"
        } catch (e: PackageManager.NameNotFoundException) {
            "Version information not available"
        }
    }
}