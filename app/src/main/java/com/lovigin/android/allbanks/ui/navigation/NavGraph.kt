package com.lovigin.android.allbanks.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lovigin.android.allbanks.ui.screens.account.AccountScreen
import com.lovigin.android.allbanks.ui.screens.home.HomeScreen
import com.lovigin.android.allbanks.ui.screens.loans.LoansScreen
import com.lovigin.android.allbanks.viewmodels.MainViewModel

enum class RootRoute(val route: String) {
    Home("home"),
    Loans("loans"),
    Account("account")
}

@Composable
fun RootNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val vm: MainViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = RootRoute.Home.route,
        modifier = modifier
    ) {
        composable(RootRoute.Home.route) { HomeScreen(vm = vm) }
        composable(RootRoute.Loans.route) { LoansScreen(vm = vm) }
        composable(RootRoute.Account.route) { AccountScreen(vm = vm) }
    }
}

fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}