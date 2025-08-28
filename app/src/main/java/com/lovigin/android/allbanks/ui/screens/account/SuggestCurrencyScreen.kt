package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.BuildConfig
import com.lovigin.android.allbanks.data.remote.TelegramApi
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestCurrencyScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // Prefill email из аккаунта
    val user by viewModel.currentUser.collectAsState()
    LaunchedEffect(user?.email) {
        user?.email?.let { email = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currency Feedback", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            // Your email
            Text("Your email", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("example@email.com") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            // Currency to suggest
            Text("Currency to suggest", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Tell us what to add…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                minLines = 6
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    isSubmitting = true
                    val message = buildString {
                        appendLine("New Currency Suggestion")
                        appendLine("From: $email")
                        appendLine()
                        append("Suggestion: $text")
                    }
                    scope.launch {
                        val ok = TelegramApi.sendMessage(
                            token = BuildConfig.TELEGRAM_BOT_TOKEN,
                            chatId = BuildConfig.TELEGRAM_CHAT_ID,
                            message = message
                        )
                        isSubmitting = false
                        if (ok) {
                            showSuccess = true
                            text = ""
                            // email очищать не будем — полезно оставить
                        } else {
                            // Можно показать Snackbar/Alert об ошибке
                        }
                    }
                },
                enabled = email.isNotBlank() && text.isNotBlank() && !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = main)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("Send")
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) { Text("OK") }
            },
            title = { Text("Thanks!") },
            text = { Text("We will get back to you soon.") }
        )
    }
}