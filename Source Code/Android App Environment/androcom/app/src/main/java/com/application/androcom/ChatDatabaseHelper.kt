// Import necessary modules
package com.application.androcom

import android.content.ContentValues
import android.content.Context
import java.util.*
import android.util.Log
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Database helper class
class ChatDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        // Database constants
        private const val DATABASE_VERSION = 4
        private const val DATABASE_NAME = "ChatDatabase.db"
        private const val TABLE_CHAT = "chat"
        private const val TABLE_MUTE = "mute"
        private const val TABLE_BLOCK = "block"
        private const val KEY_ID = "_id"
        private const val KEY_RECEIVER = "receiver"
        private const val KEY_NAME = "name"
        private const val KEY_MAC = "mac"
        private const val KEY_MESSAGE = "message"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_IS_SENT = "is_sent"
        private const val KEY_IP_ADDRESS = "ip_address"
    }

    // onCreate method creates necessary tables
    override fun onCreate(db: SQLiteDatabase) {
        createChatTable(db)
        createMuteTable(db)
        createBlockTable(db)
    }

    // Private method to create chat table
    private fun createChatTable(db: SQLiteDatabase) {
        val createTableChatQuery = "CREATE TABLE $TABLE_CHAT ($KEY_ID INTEGER PRIMARY KEY, $KEY_NAME TEXT, $KEY_MAC TEXT, $KEY_RECEIVER TEXT, $KEY_MESSAGE TEXT, $KEY_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP, $KEY_IS_SENT INTEGER)"
        db.execSQL(createTableChatQuery)
    }

    // Private method to create mute table
    private fun createMuteTable(db: SQLiteDatabase) {
        val createTableMuteQuery = "CREATE TABLE $TABLE_MUTE ($KEY_ID INTEGER PRIMARY KEY, $KEY_IP_ADDRESS TEXT)"
        db.execSQL(createTableMuteQuery)
    }

    // Private method to create block table
    private fun createBlockTable(db: SQLiteDatabase) {
        val createTableBlockQuery = "CREATE TABLE $TABLE_BLOCK ($KEY_ID INTEGER PRIMARY KEY, $KEY_IP_ADDRESS TEXT, $KEY_NAME TEXT)"
        db.execSQL(createTableBlockQuery)
    }

    // onUpgrade method drops existing tables and recreates them
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val tablesToDrop = listOf(TABLE_CHAT, TABLE_MUTE, TABLE_BLOCK)
        tablesToDrop.forEach { tableName ->
            db.execSQL("DROP TABLE IF EXISTS $tableName")
        }
        onCreate(db)
    }

    // Function to add chat message to database
    fun addChat(receiver: String, message: String, name: String,mac: String, isSent: Boolean): Boolean {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_RECEIVER, receiver)
        values.put(KEY_MESSAGE, message)
        values.put(KEY_NAME, name)
        values.put(KEY_MAC, mac)
        values.put(KEY_IS_SENT, if (isSent) 1 else 0)
        val result = db.insert(TABLE_CHAT, null, values)
        db.close()
        return result != -1L // If result is -1, insertion failed; otherwise, it succeeded
    }

    // Function to get all unique user IPs
    fun getAllUserIPs(localIp: String): ArrayList<UserIP> {
        val userIPSet = HashSet<UserIP>() // Use a set to ensure uniqueness
        val db = this.readableDatabase
//        val distinctReceiverQuery = "SELECT $KEY_NAME, $KEY_RECEIVER, $KEY_MAC FROM $TABLE_CHAT WHERE $KEY_RECEIVER != '$localIp' GROUP BY $KEY_RECEIVER"

        val distinctReceiverQuery = """
    SELECT c.$KEY_NAME, c.$KEY_RECEIVER, c.$KEY_MAC
    FROM $TABLE_CHAT c
    JOIN (
        SELECT $KEY_RECEIVER, MAX($KEY_TIMESTAMP) AS max_timestamp
        FROM $TABLE_CHAT
        WHERE $KEY_RECEIVER != '$localIp'
        GROUP BY $KEY_RECEIVER
    ) max_ts ON c.$KEY_RECEIVER = max_ts.$KEY_RECEIVER AND c.$KEY_TIMESTAMP = max_ts.max_timestamp
""".trimIndent()

        try {
            val receiverCursor = db.rawQuery(distinctReceiverQuery, null)
            receiverCursor.use { cursor ->
                while (cursor.moveToNext()) {
                    val name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                    val ip = cursor.getString(cursor.getColumnIndex(KEY_RECEIVER))
                    val mac = cursor.getString(cursor.getColumnIndex(KEY_MAC))
                    // Check if the IP matches the one to exclude
                    userIPSet.add(UserIP(name = name, IP = ip, mac = mac))
                }
            }
        } catch (e: Exception) {
            Log.e("ChatDatabaseHelper", "Error retrieving user IPs: $e")
        } finally {
            db.close()
        }
        return ArrayList(userIPSet) // Convert set back to ArrayList
    }

    // Function to get all chat messages for a specific IP
    fun getAllChatsForIp(ip: String?): ArrayList<ChatMessage> {
        val chatMessages = ArrayList<ChatMessage>()
        val db = this.readableDatabase
        val selection = "$KEY_RECEIVER = ?"
        val selectionArgs = arrayOf(ip)
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
                        // Removed references to KEY_SENDER
                        val receiver = cursor.getString(cursor.getColumnIndex(KEY_RECEIVER))
                        val message = cursor.getString(cursor.getColumnIndex(KEY_MESSAGE))
                        val timestamp = cursor.getString(cursor.getColumnIndex(KEY_TIMESTAMP))
                        val isSent = cursor.getInt(cursor.getColumnIndex(KEY_IS_SENT)) == 1
                        val chatMessage = ChatMessage( receiver, message, timestamp, isSent)
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

    // Function to add an IP address to the mute table
    fun addIPToMute(ip: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_IP_ADDRESS, ip)
        }
        val result = db.insert(TABLE_MUTE, null, values)
        db.close()
        return result != -1L
    }

    // Function to remove an IP address from the mute table
    fun removeIPFromMute(ip: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_MUTE, "$KEY_IP_ADDRESS = ?", arrayOf(ip))
        db.close()
        return result > 0
    }

    // Function to add an IP address to the block table
    fun addIPToBlock(ip: String, username: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(KEY_IP_ADDRESS, ip)
            put(KEY_NAME, username)
        }
        val result = db.insert(TABLE_BLOCK, null, values)
        db.close()
        return result != -1L
    }

    // Function to remove an IP address from the block table
    fun removeIPFromBlock(ip: String): Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_BLOCK, "$KEY_IP_ADDRESS = ?", arrayOf(ip))
        db.close()
        return result > 0
    }

    // Function to check if an IP address is muted
    fun isIPMuted(ip: String): Boolean {
        val db = this.readableDatabase
        val selection = "$KEY_IP_ADDRESS = ?"
        val selectionArgs = arrayOf(ip)
        val cursor = db.query(TABLE_MUTE, null, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }

    // Function to check if an IP address is blocked
    fun isIPBlocked(ip: String): Boolean {
        val db = this.readableDatabase
        val selection = "$KEY_IP_ADDRESS = ?"
        val selectionArgs = arrayOf(ip)
        val cursor = db.query(TABLE_BLOCK, null, selection, selectionArgs, null, null, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }


// Function to get all data from the block table
fun getAllBlockData(): HashSet<BlockData> {
    val blockDataList = HashSet<BlockData>()
    val db = this.readableDatabase
    val query = "SELECT * FROM $TABLE_BLOCK"
    val cursor = db.rawQuery(query, null)
    cursor.use { cursor ->
        while (cursor.moveToNext()) {
            val ip = cursor.getString(cursor.getColumnIndex(KEY_IP_ADDRESS))
            val username = cursor.getString(cursor.getColumnIndex(KEY_NAME))
            val blockData = BlockData(ip, username)
            blockDataList.add(blockData)
        }
    }
    db.close()
    return blockDataList
}

}

// Data class to represent a chat message
data class ChatMessage(val receiver: String, val message: String, val timestamp: String, val isSent: Boolean)

// Data class to represent data from the block table
data class BlockData(val ip: String, val username: String)
