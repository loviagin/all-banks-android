package com.lovigin.android.allbanks.ui.welcome

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.ui.graphics.vector.ImageVector

data class WelcomePage(
    val title: String,
    val subtitle: String,
    val color: Color,
    val icon: ImageVector
)

val welcomePages = listOf(
    WelcomePage(
        title = "💰 Your finances in one place",
        subtitle = "Combine all accounts, cards and crypto wallets in one app. Control your finances now in your hands.",
        color = Color(red = 0.2f, green = 0.5f, blue = 0.8f),
        icon = Icons.Outlined.AttachMoney
    ),
    WelcomePage(
        title = "🔒 Maximum privacy",
        subtitle = "Your data is stored only in your iCloud account. No servers, no analytics — only your privacy.",
        color = Color(red = 0.2f, green = 0.7f, blue = 0.3f),
        icon = Icons.Outlined.Lock
    ),
    WelcomePage(
        title = "🌍 Multi-currency control",
        subtitle = "Instant conversion of balances to any currency. Follow your assets in a convenient format for you.",
        color = Color(red = 0.6f, green = 0.2f, blue = 0.8f),
        icon = Icons.Outlined.Public
    )
)