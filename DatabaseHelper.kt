package com.vampire.browser.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "vampire.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE bookmarks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, createdAt INTEGER)")
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, linkedUrl TEXT, updatedAt INTEGER)")
        db.execSQL("CREATE TABLE history (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, visitedAt INTEGER)")
        db.execSQL("CREATE TABLE downloads (id INTEGER PRIMARY KEY AUTOINCREMENT, fileName TEXT, url TEXT, destinationPath TEXT, downloadedBytes INTEGER, totalBytes INTEGER, status TEXT, isPrivate INTEGER, createdAt INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {
        db.execSQL("DROP TABLE IF EXISTS bookmarks")
        db.execSQL("DROP TABLE IF EXISTS notes")
        db.execSQL("DROP TABLE IF EXISTS history")
        db.execSQL("DROP TABLE IF EXISTS downloads")
        onCreate(db)
    }

    suspend fun addBookmark(title: String, url: String) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", title.ifBlank { url })
            put("url", url)
            put("createdAt", System.currentTimeMillis())
        }
        writableDatabase.insert("bookmarks", null, cv)
    }

    suspend fun getBookmarks(): List<Bookmark> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Bookmark>()
        val cursor = readableDatabase.query("bookmarks", null, null, null, null, null, "createdAt DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(Bookmark(it.getLong(0), it.getString(1), it.getString(2), it.getLong(3)))
            }
        }
        list
    }

    suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("bookmarks", "id = ?", arrayOf(id.toString()))
    }

    suspend fun saveNote(title: String, content: String, linkedUrl: String? = null) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", title)
            put("content", content)
            put("linkedUrl", linkedUrl)
            put("updatedAt", System.currentTimeMillis())
        }
        writableDatabase.insert("notes", null, cv)
    }

    suspend fun getNotes(): List<Note> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Note>()
        val cursor = readableDatabase.query("notes", null, null, null, null, null, "updatedAt DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(Note(it.getLong(0), it.getString(1), it.getString(2), it.getString(3), it.getLong(4)))
            }
        }
        list
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("notes", "id = ?", arrayOf(id.toString()))
    }

    suspend fun addHistory(title: String, url: String) = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("title", title)
            put("url", url)
            put("visitedAt", System.currentTimeMillis())
        }
        writableDatabase.insert("history", null, cv)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        writableDatabase.delete("history", null, null)
    }

    suspend fun insertOrUpdateDownload(record: DownloadRecord): Long = withContext(Dispatchers.IO) {
        val cv = ContentValues().apply {
            put("fileName", record.fileName)
            put("url", record.url)
            put("destinationPath", record.destinationPath)
            put("downloadedBytes", record.downloadedBytes)
            put("totalBytes", record.totalBytes)
            put("status", record.status.name)
            put("isPrivate", if (record.isPrivate) 1 else 0)
            put("createdAt", record.createdAt)
        }
        if (record.id > 0) {
            writableDatabase.update("downloads", cv, "id = ?", arrayOf(record.id.toString()))
            record.id
        } else {
            writableDatabase.insert("downloads", null, cv)
        }
    }

    suspend fun getDownloads(): List<DownloadRecord> = withContext(Dispatchers.IO) {
        val list = mutableListOf<DownloadRecord>()
        val cursor = readableDatabase.query("downloads", null, null, null, null, null, "createdAt DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    DownloadRecord(
                        id = it.getLong(0),
                        fileName = it.getString(1),
                        url = it.getString(2),
                        destinationPath = it.getString(3),
                        downloadedBytes = it.getLong(4),
                        totalBytes = it.getLong(5),
                        status = DownloadStatus.valueOf(it.getString(6)),
                        isPrivate = it.getInt(7) == 1,
                        createdAt = it.getLong(8)
                    )
                )
            }
        }
        list
    }

    suspend fun deleteDownload(id: Long) = withContext(Dispatchers.IO) {
        writableDatabase.delete("downloads", "id = ?", arrayOf(id.toString()))
    }
}