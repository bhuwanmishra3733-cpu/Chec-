package com.vampire.browser.data

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vampire_prefs", Context.MODE_PRIVATE)

    var defaultSearchEngine: String
        get() = prefs.getString("search_engine", "https://www.google.com/search?q=") ?: "https://www.google.com/search?q="
        set(value) = prefs.edit().putString("search_engine", value).apply()

    var homePage: String
        get() = prefs.getString("home_page", "https://www.google.com") ?: "https://www.google.com"
        set(value) = prefs.edit().putString("home_page", value).apply()

    var vaultPinHash: String?
        get() = prefs.getString("vault_pin_hash", null)
        set(value) = prefs.edit().putString("vault_pin_hash", value).apply()

    var vaultPinSalt: String?
        get() = prefs.getString("vault_pin_salt", null)
        set(value) = prefs.edit().putString("vault_pin_salt", value).apply()

    var lastBackgroundTimestamp: Long
        get() = prefs.getLong("last_background_timestamp", 0L)
        set(value) = prefs.edit().putLong("last_background_timestamp", value).apply()

    fun isVaultSetup(): Boolean = vaultPinHash != null && vaultPinSalt != null
}