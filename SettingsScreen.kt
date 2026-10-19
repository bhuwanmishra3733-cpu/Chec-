package com.vampire.browser.ui.screens

import android.webkit.CookieManager
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vampire.browser.data.DatabaseHelper
import com.vampire.browser.data.PreferenceManager
import com.vampire.browser.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(prefs: PreferenceManager, dbHelper: DatabaseHelper, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var done by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack).padding(16.dp)) {
            Text("Privacy Controls", color = VampireCrimson, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    scope.launch {
                        dbHelper.clearHistory()
                        val wv = WebView(context)
                        wv.clearCache(true)
                        wv.destroy()
                        CookieManager.getInstance().removeAllCookies(null)
                        done = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VampireCard)
            ) {
                Text("Clear History, Cache & Cookies", color = VampireCrimson)
            }
            if (done) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Cleared successfully!", color = VampireSuccess)
            }
        }
    }
}