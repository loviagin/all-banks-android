package com.lovigin.android.allbanks.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.compose.material3.MaterialTheme
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
                ContentScreen(viewModel = vm)
            }
        }
    }
}