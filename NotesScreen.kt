package com.vampire.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vampire.browser.data.DatabaseHelper
import com.vampire.browser.data.Note
import com.vampire.browser.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(dbHelper: DatabaseHelper, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { notes = dbHelper.getNotes() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Notes", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = VampireCrimson, contentColor = VampireBlack) {
                Icon(Icons.Default.Add, contentDescription = "New")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack).padding(12.dp)) {
            items(notes) { n ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = VampireCard)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(n.title, color = VampireTextPrimary)
                            Text(n.content, color = VampireTextSecondary)
                        }
                        IconButton(onClick = { scope.launch { dbHelper.deleteNote(n.id); notes = dbHelper.getNotes() } }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = VampireTextSecondary)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Note") },
                text = {
                    Column {
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (title.isNotBlank()) {
                            scope.launch {
                                dbHelper.saveNote(title, content)
                                notes = dbHelper.getNotes()
                                showDialog = false
                                title = ""
                                content = ""
                            }
                        }
                    }) { Text("Save") }
                }
            )
        }
    }
}