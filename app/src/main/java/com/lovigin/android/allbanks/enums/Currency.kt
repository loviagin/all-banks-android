package com.lovigin.android.allbanks.enums

import androidx.annotation.StringRes
import com.lovigin.android.allbanks.R

enum class Currency(
    val code: String,
    @StringRes val displayNameRes: Int,
    val symbol: String,
    val isCrypto: Boolean = false
) {
    USD("USD", R.string.curr_usd, "$"),
    EUR("EUR", R.string.curr_eur, "€"),
    GBP("GBP", R.string.curr_gbp, "£"),
    RUB("RUB", R.string.curr_rub, "₽"),
    BYN("BYN", R.string.curr_byn, "Br"),
    KZT("KZT", R.string.curr_kzt, "₸"),
    CNY("CNY", R.string.curr_cny, "¥"),
    JPY("JPY", R.string.curr_jpy, "¥"),
    CHF("CHF", R.string.curr_chf, "CHF"),
    SEK("SEK", R.string.curr_sek, "kr"),
    KRW("KRW", R.string.curr_krw, "₩"),
    NOK("NOK", R.string.curr_nok, "kr"),
    MXN("MXN", R.string.curr_mxn, "Mex$"),
    TRY_("TRY", R.string.curr_try, "₺");

    val id: String get() = code

    companion object {
        val all: List<Currency> = entries.toList()

        fun fromCode(code: String): Currency? =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) }
    }
}