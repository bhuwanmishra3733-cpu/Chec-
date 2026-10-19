package com.vampire.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vampire.browser.data.DatabaseHelper
import com.vampire.browser.data.DownloadRecord
import com.vampire.browser.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(dbHelper: DatabaseHelper, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var downloads by remember { mutableStateOf<List<DownloadRecord>>(emptyList()) }

    LaunchedEffect(Unit) { downloads = dbHelper.getDownloads() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary) }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack).padding(12.dp)) {
            items(downloads) { d ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = VampireCard)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(d.fileName, color = VampireTextPrimary)
                            Text(if (d.isPrivate) "Vault File" else "Public Download", color = VampireTextSecondary)
                        }
                        IconButton(onClick = { scope.launch { dbHelper.deleteDownload(d.id); downloads = dbHelper.getDownloads() } }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = VampireTextSecondary)
                        }
                    }
                }
            }
        }
    }
}