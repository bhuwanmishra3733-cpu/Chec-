package com.vampire.browser.data

data class Bookmark(
    val id: Long = 0,
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Note(
    val id: Long = 0,
    val title: String,
    val content: String,
    val linkedUrl: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class DownloadStatus {
    DOWNLOADING, COMPLETED, FAILED, CANCELLED
}

data class DownloadRecord(
    val id: Long = 0,
    val fileName: String,
    val url: String,
    val destinationPath: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val status: DownloadStatus,
    val isPrivate: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

data class VaultFile(
    val file: java.io.File,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val extension: String
)