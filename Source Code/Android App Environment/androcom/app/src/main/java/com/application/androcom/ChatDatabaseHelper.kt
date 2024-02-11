package com.application.androcom

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ChatDatabase.db"
        private const val TABLE_CHAT = "chat"
        private const val KEY_ID = "_id"
        private const val KEY_SENDER = "sender"
        private const val KEY_RECEIVER = "receiver"
        private const val KEY_MESSAGE = "message"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_IS_SENT = "is_sent"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = "CREATE TABLE $TABLE_CHAT ($KEY_ID INTEGER PRIMARY KEY, $KEY_SENDER TEXT, $KEY_RECEIVER TEXT, $KEY_MESSAGE TEXT, $KEY_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP, $KEY_IS_SENT INTEGER)"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CHAT")
        onCreate(db)
    }

    fun addChat(sender: String, receiver: String?, message: String, timestamp: String, isSent: Boolean): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_SENDER, sender)
        values.put(KEY_RECEIVER, receiver)
        values.put(KEY_MESSAGE, message)
        values.put(KEY_TIMESTAMP, timestamp)
        values.put(KEY_IS_SENT, if (isSent) 1 else 0)
        val result = db.insert(TABLE_CHAT, null, values)
        db.close()
        return result != -1L // If result is -1, insertion failed; otherwise, it succeeded
    }

    fun getAllUserIPs(localIp: String): ArrayList<UserIP> {
        val userIPArray = ArrayList<UserIP>()
        val db = this.readableDatabase
        val distinctReceiverQuery = "SELECT DISTINCT $KEY_RECEIVER FROM $TABLE_CHAT"

        try {
            val receiverCursor = db.rawQuery(distinctReceiverQuery, null)

            if (receiverCursor != null) {
                if (receiverCursor.moveToFirst()) {
                    do {
                        val receiverIP = receiverCursor.getString(receiverCursor.getColumnIndex(KEY_RECEIVER))
                        // Check if the IP matches the one to exclude
                        if (receiverIP != localIp) {
                            userIPArray.add(UserIP(receiverIP, receiverIP))
                        }
                    } while (receiverCursor.moveToNext())
                }
                receiverCursor.close()
            }
        } catch (e: Exception) {
            Log.e("ChatDatabaseHelper", "Error retrieving user IPs: $e")
        } finally {
            db.close()
        }

        return userIPArray
    }



    fun getAllChatsForIp(ip: String?): ArrayList<ChatMessage> {
        val chatMessages = ArrayList<ChatMessage>()
        val db = this.readableDatabase
        val selection = "$KEY_SENDER = ? OR $KEY_RECEIVER = ?"
        val selectionArgs = arrayOf(ip, ip)
        val cursor = db.query(
            TABLE_CHAT,
            null,
            selection,
            selectionArgs,
            null,
            null,
            KEY_TIMESTAMP
        )

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        val sender = cursor.getString(cursor.getColumnIndex(KEY_SENDER))
                        val receiver = cursor.getString(cursor.getColumnIndex(KEY_RECEIVER))
                        val message = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE))
                        val timestamp = cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP))
                        val isSent = cursor.getInt(cursor.getColumnIndex(KEY_IS_SENT)) == 1
                        val chatMessage = ChatMessage(sender, receiver, message, timestamp, isSent)
                        chatMessages.add(chatMessage)
                    } while (cursor.moveToNext())
                } else {
                    Log.d("ChatDatabaseHelper", "No chat messages found for IP: $ip")
                }
            } catch (e: Exception) {
                Log.e("ChatDatabaseHelper", "Error retrieving chat messages: $e")
            } finally {
                cursor.close()
            }
        } else {
            Log.e("ChatDatabaseHelper", "Cursor is null")
        }

        db.close()
        return chatMessages
    }

}

data class ChatMessage(val sender: String, val receiver: String, val message: String, val timestamp: String, val isSent: Boolean)
