package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.UserEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import com.lovigin.android.allbanks.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    // Текущий пользователь (из VM и/или из базы)
    val userFromVm by viewModel.currentUser.collectAsState()
    val userFromDb by db.userDao().observeFirst().collectAsState(initial = null)

    // Локальные состояния (порт @State)
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Аналог onAppear()
    LaunchedEffect(userFromVm, userFromDb) {
        val u = userFromVm ?: userFromDb
        if (u != null) {
            name = u.name
            email = u.email
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Account", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Your name", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Your name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Text("Your email", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Your email") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    // onSave()
                    scope.launch {
                        val current = userFromVm ?: userFromDb
                        val entity = if (current != null) {
                            current.copy(name = name, email = email)
                        } else {
                            UserEntity(
                                id = UUID.randomUUID(),
                                name = name,
                                email = email
                            )
                        }
                        db.userDao().upsert(entity)
                        // Обновляем VM (как делал Swift-код)
                        viewModel.setCurrentUser(entity) // ← см. helper ниже
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = main)
            ) {
                Text("Save")
            }
        }
    }
}