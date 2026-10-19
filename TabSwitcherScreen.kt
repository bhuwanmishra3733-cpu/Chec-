package com.vampire.browser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vampire.browser.browser.TabManager
import com.vampire.browser.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    tabManager: TabManager,
    onTabSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    val tabs by tabManager.tabs.collectAsState()
    val activeTabId by tabManager.activeTabId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tabs (${tabs.size})", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                actions = {
                    IconButton(onClick = { tabManager.closeAllTabs() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Close All", tint = VampireTextSecondary)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VampireTextPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val t = tabManager.createTab("https://www.google.com", false)
                    onTabSelected(t.id)
                },
                containerColor = VampireCrimson,
                contentColor = VampireBlack
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Tab")
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tabs) { tab ->
                val isActive = tab.id == activeTabId
                Card(
                    modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onTabSelected(tab.id) },
                    colors = CardDefaults.cardColors(containerColor = if (tab.isPrivate) VampirePrivateSurface else VampireCard),
                    border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, VampireCrimson) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (tab.isPrivate) "[P] " + tab.title else tab.title, color = VampireTextPrimary, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f))
                            IconButton(onClick = { tabManager.closeTab(tab.id) }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = VampireTextSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tab.url, color = VampireTextSecondary, fontSize = 11.sp, maxLines = 2)
                    }
                }
            }
        }
    }
}