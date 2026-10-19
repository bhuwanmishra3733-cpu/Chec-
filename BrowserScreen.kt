package com.vampire.browser.ui.screens

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.vampire.browser.browser.TabManager
import com.vampire.browser.ui.theme.*

@Composable
fun BrowserScreen(
    tabManager: TabManager,
    onOpenTabSwitcher: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenVault: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val tabs by tabManager.tabs.collectAsState()
    val activeTabId by tabManager.activeTabId.collectAsState()
    val currentTab = tabs.find { it.id == activeTabId }

    var urlInput by remember(currentTab?.url) { mutableStateOf(currentTab?.url ?: "") }
    var showMenu by remember { mutableStateOf(false) }

    fun navigate(input: String) {
        val target = input.trim()
        val finalUrl = if (target.startsWith("http://") || target.startsWith("https://")) {
            target
        } else if (target.contains(".") && !target.contains(" ")) {
            "https://$target"
        } else {
            "https://www.google.com/search?q=${java.net.URLEncoder.encode(target, "UTF-8")}"
        }
        currentTab?.webView?.loadUrl(finalUrl)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (currentTab?.isPrivate == true) VampirePrivateSurface else VampireSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(VampireCard)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = VampireTextPrimary,
                                    unfocusedTextColor = VampireTextPrimary,
                                    cursorColor = VampireCrimson,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = { navigate(urlInput) }),
                                placeholder = { Text("Search or type URL", color = VampireTextSecondary, fontSize = 14.sp) }
                            )
                            if (currentTab?.isLoading == true) {
                                IconButton(onClick = { currentTab.webView?.stopLoading() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Stop", tint = VampireTextSecondary)
                                }
                            } else {
                                IconButton(onClick = { currentTab?.webView?.reload() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = VampireTextSecondary)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VampireCard)
                            .clickable { onOpenTabSwitcher() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tabs.size.toString(), color = VampireTextPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = VampireTextPrimary)
                    }
                }
                if (currentTab?.isLoading == true) {
                    LinearProgressIndicator(
                        progress = { currentTab.progress / 100f },
                        modifier = Modifier.fillMaxWidth().height(2.dp).padding(top = 4.dp),
                        color = VampireCrimson,
                        trackColor = Color.Transparent
                    )
                }
            }
        },
        bottomBar = {
            Surface(modifier = Modifier.fillMaxWidth(), color = VampireSurface) {
                Row(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { currentTab?.webView?.goBack() }, enabled = currentTab?.canGoBack == true) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary)
                    }
                    IconButton(onClick = { currentTab?.webView?.goForward() }, enabled = currentTab?.canGoForward == true) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Forward", tint = VampireTextPrimary)
                    }
                    IconButton(onClick = { currentTab?.webView?.loadUrl("https://www.google.com") }) {
                        Icon(Icons.Default.Home, contentDescription = "Home", tint = VampireTextPrimary)
                    }
                    IconButton(onClick = onOpenBookmarks) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmarks", tint = VampireTextPrimary)
                    }
                    IconButton(onClick = { tabManager.createTab("https://www.google.com", false) }) {
                        Icon(Icons.Default.Add, contentDescription = "New Tab", tint = VampireCrimson)
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack)) {
            if (currentTab != null && currentTab.webView != null) {
                AndroidView(
                    factory = {
                        val wv = currentTab.webView!!
                        (wv.parent as? ViewGroup)?.removeView(wv)
                        wv
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(VampireCard)) {
                DropdownMenuItem(
                    text = { Text("New Private Tab", color = VampireTextPrimary) },
                    onClick = { showMenu = false; tabManager.createTab("https://www.google.com", true) }
                )
                DropdownMenuItem(
                    text = { Text(if (currentTab?.isDesktopMode == true) "Mobile View" else "Desktop View", color = VampireTextPrimary) },
                    onClick = { showMenu = false; currentTab?.let { tabManager.toggleDesktopMode(it) } }
                )
                DropdownMenuItem(text = { Text("Downloads", color = VampireTextPrimary) }, onClick = { showMenu = false; onOpenDownloads() })
                DropdownMenuItem(text = { Text("Private Vault", color = VampireTextPrimary) }, onClick = { showMenu = false; onOpenVault() })
                DropdownMenuItem(text = { Text("Notes", color = VampireTextPrimary) }, onClick = { showMenu = false; onOpenNotes() })
                DropdownMenuItem(text = { Text("Settings", color = VampireTextPrimary) }, onClick = { showMenu = false; onOpenSettings() })
            }
        }
    }
}