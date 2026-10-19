package com.vampire.browser.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vampire.browser.data.PreferenceManager
import com.vampire.browser.data.VaultFile
import com.vampire.browser.security.SecurityUtils
import com.vampire.browser.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(context: Context, prefs: PreferenceManager, onBack: () -> Unit) {
    var isUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(true) }
    var enteredPin by remember { mutableStateOf("") }
    var setupFirstPin by remember { mutableStateOf("") }
    var promptTitle by remember { mutableStateOf(if (prefs.isVaultSetup()) "Enter Vault PIN" else "Create 4-6 Digit PIN") }

    val vaultDir = remember { File(context.filesDir, "vault").apply { if (!exists()) mkdirs() } }
    var files by remember { mutableStateOf<List<VaultFile>>(emptyList()) }

    fun refresh() {
        files = vaultDir.listFiles()?.map {
            VaultFile(it, it.name, it.length(), it.lastModified(), it.isDirectory, it.extension)
        }?.sortedByDescending { it.lastModified } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Vault", color = VampireTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = VampireSurface),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = VampireTextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(VampireBlack)) {
            if (isUnlocked) {
                if (files.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Private Vault is Empty", color = VampireTextSecondary)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        items(files) { f ->
                            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = VampireCard)) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = VampireCrimson)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(f.name, color = VampireTextPrimary, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { f.file.delete(); refresh() }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = VampireTextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showPinDialog) {
                Dialog(onDismissRequest = { if (!isUnlocked) onBack() }) {
                    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = VampireCard), shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(promptTitle, style = MaterialTheme.typography.titleMedium, color = VampireTextPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = enteredPin,
                                onValueChange = { if (it.length <= 6) enteredPin = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (enteredPin.length >= 4) {
                                        if (!prefs.isVaultSetup()) {
                                            if (setupFirstPin.isEmpty()) {
                                                setupFirstPin = enteredPin
                                                enteredPin = ""
                                                promptTitle = "Confirm Vault PIN"
                                            } else {
                                                if (setupFirstPin == enteredPin) {
                                                    val (h, s) = SecurityUtils.hashPin(enteredPin)
                                                    prefs.vaultPinHash = h
                                                    prefs.vaultPinSalt = s
                                                    showPinDialog = false
                                                    isUnlocked = true
                                                    refresh()
                                                } else {
                                                    setupFirstPin = ""
                                                    enteredPin = ""
                                                    promptTitle = "PIN Mismatch! Try Again"
                                                }
                                            }
                                        } else {
                                            if (SecurityUtils.verifyPin(enteredPin, prefs.vaultPinHash ?: "", prefs.vaultPinSalt ?: "")) {
                                                showPinDialog = false
                                                isUnlocked = true
                                                refresh()
                                            } else {
                                                enteredPin = ""
                                                promptTitle = "Incorrect PIN!"
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VampireCrimson)
                            ) {
                                Text("Enter")
                            }
                        }
                    }
                }
            }
        }
    }
}