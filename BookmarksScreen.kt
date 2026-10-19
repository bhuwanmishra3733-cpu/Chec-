package com.vampire.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.vampire.browser.data.Bookmark
import com.vampire.browser.data.DatabaseHelper
import com.vampire.browser.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(dbHelper: DatabaseHelper, onSelectBookmark: (String) -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var bookmarks by remember { mutableStateOf<List<Bookmark>>(emptyList()) }

    LaunchedEffect(Unit) { bookmarks = dbHelper.getBookmarks() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bookmarks", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary) }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack).padding(12.dp)) {
            items(bookmarks) { b ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelectBookmark(b.url) }, colors = CardDefaults.cardColors(containerColor = VampireCard)) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(b.title, color = VampireTextPrimary)
                            Text(b.url, color = VampireTextSecondary)
                        }
                        IconButton(onClick = { scope.launch { dbHelper.deleteBookmark(b.id); bookmarks = dbHelper.getBookmarks() } }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = VampireTextSecondary)
                        }
                    }
                }
            }
        }
    }
}