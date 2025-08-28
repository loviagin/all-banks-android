package com.lovigin.android.allbanks.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.lovigin.android.allbanks.data.local.AppDatabase
import com.lovigin.android.allbanks.data.local.entity.CategoryEntity
import com.lovigin.android.allbanks.ui.theme.Brand
import com.lovigin.android.allbanks.ui.theme.MainDark
import com.lovigin.android.allbanks.ui.theme.MainLight
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen() {
    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val scope = rememberCoroutineScope()

    val categories by db.categoryDao().observeAll().collectAsState(initial = emptyList())

    val dark = isSystemInDarkTheme()
    val main = if (dark) MainDark else MainLight

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", color = main) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brand),
                actions = {
                    TextButton(onClick = { newName = ""; showAddDialog = true }) {
                        Text("Add new", color = main)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { newName = ""; showAddDialog = true },
                containerColor = Brand,
                contentColor = main
            ) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (categories.isEmpty()) {
                // Пустое состояние + кнопка
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .background(Brand.copy(alpha = 0.06f), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp)
                ) {
                    Text("No categories yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { newName = ""; showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = main)
                    ) { Text("Add new category") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(categories, key = { it.id }) { cat ->
                        CategoryRow(
                            category = cat,
                            onDelete = {
                                scope.launch { db.categoryDao().delete(cat) }
                            },
                            onEdit = { /* на будущее: экран редактирования; сейчас не обязателен */ }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add new Category") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = { Text("Category name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isNotBlank()) {
                        val entity = CategoryEntity(
                            id = UUID.randomUUID(),
                            name = newName.trim(),
                            icon = "" // пока не используем
                        )
                        scope.launch {
                            db.categoryDao().upsert(entity)
                            showAddDialog = false
                            newName = ""
                        }
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Label, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}