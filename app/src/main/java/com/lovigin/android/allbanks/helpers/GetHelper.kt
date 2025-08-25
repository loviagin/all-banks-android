package com.lovigin.android.allbanks.helpers

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.vector.ImageVector
import com.lovigin.android.allbanks.R
import com.lovigin.android.allbanks.enums.Currency
import com.lovigin.android.allbanks.ui.icons.bankIconForCurrencyCode
import java.util.UUID

object GetHelper {

    private const val TAG = "GetHelper"

    /** Swift: getCurrency(_:) */
    fun getCurrency(code: String): Currency {
        return Currency.fromCode(code)
            ?: run {
                Log.e(TAG, "ERROR currency code: $code")
                Currency.USD
            }
    }

    /**
     * Swift: getAccountsCount(for:accounts:)
     * В iOS у тебя сравнение по bank.id ↔︎ account.bankId.
     * Дам две перегрузки: по UUID и по строковому коду банка.
     */

    // Если у аккаунта есть bankId: UUID
    fun <T> getAccountsCount(bankId: UUID, accounts: List<T>, selector: (T) -> UUID?): Int {
        return accounts.count { selector(it) == bankId }
    }

    // Если у аккаунта хранится строковый код банка (например, "tinkoff", "revolut" и т.п.)
    fun <T> getAccountsCount(bankCode: String, accounts: List<T>, selector: (T) -> String?): Int {
        return accounts.count { selector(it)?.equals(bankCode, ignoreCase = true) == true }
    }

    /**
     * Swift: getBankIcon(currency: String?) -> String (SF Symbols)
     * На Android вернём ИД ресурса иконки (VectorDrawable).
     * Создай в проекте соответствующие векторные иконки:
     *  - ic_bank_usd, ic_bank_eur, ic_bank_gbp, ic_bank_rub, ic_bank_cny, ic_bank_jpy,
     *  - ic_bank_chf, ic_bank_sek, ic_bank_krw, ic_bank_nok, ic_bank_mxn, ic_bank_try,
     *  - ic_bank_generic (фолбэк)
     * Или замапь на Material Symbols — просто поменяй return на нужные drawable.
     */
    fun getBankIcon(currencyCode: String?): ImageVector {
        return bankIconForCurrencyCode(currencyCode)
    }

    /** Swift: getAppVersion() — читаем версия/сборка из PackageInfo */
    fun getAppVersion(context: Context): String {
        return try {
            val pm = context.packageManager
            val pkg = context.packageName
            val info = pm.getPackageInfo(pkg, 0)
            val versionName = info.versionName ?: "Unknown"
            // longVersionCode доступен с P; для обратной совместимости:
            val buildNumber = if (android.os.Build.VERSION.SDK_INT >= 28) {
                info.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toString()
            }
            "Version $versionName ($buildNumber)"
        } catch (e: Exception) {
            Log.e(TAG, "Version read failed", e)
            "Version information not available"
        }
    }
}