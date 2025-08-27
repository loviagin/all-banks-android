package com.lovigin.android.allbanks.model

enum class Currency(
    val code: String,
    val displayName: String,
    val symbol: String,
    val isCrypto: Boolean = false
) {
    USD("USD", "US Dollar", "$"),
    EUR("EUR", "Euro", "€"),
    GBP("GBP", "British Pound", "£"),
    RUB("RUB", "Russian Ruble", "₽"),
    BYN("BYN", "Belarusian Ruble", "Br"),
    KZT("KZT", "Tenge", "₸"),
    CNY("CNY", "Chinese Yuan", "¥"),
    JPY("JPY", "Japanese Yen", "¥"),
    CHF("CHF", "Swiss Franc", "CHF"),
    SEK("SEK", "Swedish Krona", "kr"),
    KRW("KRW", "South Korean Won", "₩"),
    NOK("NOK", "Norwegian Krone", "kr"),
    MXN("MXN", "Mexican Peso", "Mex$"),
    TRY_("TRY", "Turkish Lira", "₺");

    companion object {
        fun fromCode(code: String): Currency? = entries.find { it.code == code }
    }
}