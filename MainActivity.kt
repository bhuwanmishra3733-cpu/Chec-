package com.vampire.browser

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.vampire.browser.browser.TabManager
import com.vampire.browser.data.DatabaseHelper
import com.vampire.browser.data.PreferenceManager
import com.vampire.browser.ui.screens.*
import com.vampire.browser.ui.theme.VampireTheme

enum class Screen {
    BROWSER, TAB_SWITCHER, DOWNLOADS, VAULT, BOOKMARKS, NOTES, SETTINGS
}

class MainActivity : FragmentActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefs: PreferenceManager
    private lateinit var tabManager: TabManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)
        prefs = PreferenceManager(this)
        tabManager = TabManager(this, dbHelper, lifecycleScope)

        if (tabManager.tabs.value.isEmpty()) {
            tabManager.createTab(prefs.homePage, false)
        }

        setContent {
            VampireTheme {
                var currentScreen by remember { mutableStateOf(Screen.BROWSER) }

                when (currentScreen) {
                    Screen.BROWSER -> BrowserScreen(
                        tabManager = tabManager,
                        onOpenTabSwitcher = { currentScreen = Screen.TAB_SWITCHER },
                        onOpenBookmarks = { currentScreen = Screen.BOOKMARKS },
                        onOpenDownloads = { currentScreen = Screen.DOWNLOADS },
                        onOpenVault = { currentScreen = Screen.VAULT },
                        onOpenNotes = { currentScreen = Screen.NOTES },
                        onOpenSettings = { currentScreen = Screen.SETTINGS }
                    )
                    Screen.TAB_SWITCHER -> TabSwitcherScreen(
                        tabManager = tabManager,
                        onTabSelected = {
                            tabManager.selectTab(it)
                            currentScreen = Screen.BROWSER
                        },
                        onClose = { currentScreen = Screen.BROWSER }
                    )
                    Screen.DOWNLOADS -> DownloadsScreen(
                        dbHelper = dbHelper,
                        onBack = { currentScreen = Screen.BROWSER }
                    )
                    Screen.VAULT -> VaultScreen(
                        context = this,
                        prefs = prefs,
                        onBack = { currentScreen = Screen.BROWSER }
                    )
                    Screen.BOOKMARKS -> BookmarksScreen(
                        dbHelper = dbHelper,
                        onSelectBookmark = { url ->
                            tabManager.activeTab()?.webView?.loadUrl(url)
                            currentScreen = Screen.BROWSER
                        },
                        onBack = { currentScreen = Screen.BROWSER }
                    )
                    Screen.NOTES -> NotesScreen(
                        dbHelper = dbHelper,
                        onBack = { currentScreen = Screen.BROWSER }
                    )
                    Screen.SETTINGS -> SettingsScreen(
                        prefs = prefs,
                        dbHelper = dbHelper,
                        onBack = { currentScreen = Screen.BROWSER }
                    )
                }
            }
        }
    }
}