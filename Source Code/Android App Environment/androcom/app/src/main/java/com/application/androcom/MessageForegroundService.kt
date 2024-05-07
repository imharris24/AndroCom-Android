package com.application.androcom

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket

class MessageForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val NOTIFICATION_TAG = "MessageForegroundServiceTag"
        const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService","Started")
        createNotificationChannel()

        val notification = createNotification()

//        startForeground(NOTIFICATION_ID, notification)

        // Start message receiver in a background thread
        startMessageReceiver()

        return START_STICKY
    }

    // function that receives the message from message socket
    @RequiresApi(Build.VERSION_CODES.O)
    fun startMessageReceiver() {

        // create instance for encryption method
        val aes = AESEncryption()

        Thread {
            try {
                // socket programming
                val serverSocket = ServerSocket(SocketPorts.MESSAGE_PORT)
                while (true) {

                    val clientSocket = serverSocket.accept()
                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    var message = reader.readLine()

                    // decrypt message
                    message = aes.decrypt(message, "AndroCom")
                    Log.i("activity line:191", message)
//                    val parts = message.split(": ", limit = 2)
//                    val senderIp = parts[0]
//                    val splitMessage = parts[1]


                    val startIndex = message.indexOf('{') + 1
                    val endIndex = message.indexOf('}')
                    val ipAndName = message.substring(startIndex, endIndex)
                    val splitMessage = message.substring(endIndex + 3)

                    val parts = ipAndName.split(":")
                    val senderIp = parts[0]
                    val senderName = parts[1]
                    val senderMac = parts[2]

                    //block user check
                    if ( !ChatDatabaseHelper(this).isIPBlocked(senderIp)) {

                        //mute user check
                        if (!ChatDatabaseHelper(this).isIPMuted(senderIp)) {
//                            Log.d("mute","mute this message ${ChatDatabaseHelper(this).isIPMuted(senderIp)}")
                            showNotification(senderName, splitMessage)
                        }
                        Log.i(
                            "Bug",
                            "Message is recieved \nSender: ${senderName}, Message: ${splitMessage}"
                        )


                        val receivedMessage = Message(splitMessage, false)
                        // save message to SQLite database

                        saveMessage(
                            message = receivedMessage,
                            receiverIp = senderIp,
                            name = senderName,
                            mac = senderMac,
                            isSent = false
                        )

                    }
                    clientSocket.close()
                }
            } catch (e: IOException) {
                Log.e("Exception", "error receiving message: $e")
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Message Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, SplashScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            /* context = */ this,
            /* requestCode = */ 0,
            /* intent = */ notificationIntent,
            /* flags = */ PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Message Foreground Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showNotification(senderIp: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(senderIp)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_TAG, 0, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMessage(message: Message, /*senderIp: String,*/ receiverIp: String, isSent: Boolean,name: String,mac:String) {

        val dbHelper = ChatDatabaseHelper(this)
        val isSaved = dbHelper.addChat( receiver = receiverIp, message = message.text, name = name,mac=mac, isSent = isSent)

        // if message is saved in database
        if (isSaved) {
            Log.d("saveMessage", "Data saved successfully")
        } else {
            Log.d("saveMessage", "Failed to save data")
        }
    }
}
