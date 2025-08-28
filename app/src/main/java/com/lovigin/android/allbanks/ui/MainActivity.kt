package com.lovigin.android.allbanks.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lovigin.android.allbanks.data.prefs.hasSeenWelcome
import com.lovigin.android.allbanks.data.prefs.setHasSeenWelcome
import com.lovigin.android.allbanks.ui.screens.welcome.WelcomeScreen
import com.lovigin.android.allbanks.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vm = ViewModelProvider(
            this,
            MainViewModel.factory(applicationContext)
        )[MainViewModel::class.java]

        setContent {
            MaterialTheme {
                var showWelcome by remember { mutableStateOf(!hasSeenWelcome(this)) }

                if (showWelcome) {
                    WelcomeScreen(
                        onFinish = {
                            setHasSeenWelcome(this, true)
                            showWelcome = false
                        }
                    )
                } else {
                    ContentScreen(viewModel = vm)
                }
            }
        }
    }
}