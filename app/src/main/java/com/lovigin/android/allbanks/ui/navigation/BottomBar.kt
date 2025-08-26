package com.lovigin.android.allbanks.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Person

data class BottomItem(val route: RootRoute, val title: String, val icon: ImageVector)

private val items = listOf(
    BottomItem(RootRoute.Home, "Home", Icons.Outlined.Home),
    BottomItem(RootRoute.Loans, "Loans", Icons.Outlined.CreditCard),
    BottomItem(RootRoute.Account, "Account", Icons.Outlined.Person)
)

@Composable
fun BottomBar(nav: NavHostController, currentRoute: String?) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route.route,
                onClick = { nav.navigateSingleTop(item.route.route) },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) }
            )
        }
    }
}