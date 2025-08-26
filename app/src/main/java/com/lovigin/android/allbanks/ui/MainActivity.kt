package com.lovigin.android.allbanks.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lovigin.android.allbanks.ui.navigation.BottomBar
import com.lovigin.android.allbanks.ui.navigation.RootNavHost
import com.lovigin.android.allbanks.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AllBanksAppContent() }
    }
}

@Composable
fun AllBanksAppContent() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val vm: MainViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    // аналог твоего ContentView.getUser()
    LaunchedEffect(Unit) {
        scope.launch { vm.ensureUser() }
    }

    Scaffold(
        bottomBar = {
            BottomBar(nav = nav, currentRoute = currentRoute)
        }
    ) { padding ->
        RootNavHost(navController = nav, modifier = Modifier.padding(padding))
    }
}