package com.application.androcom

// Import statements
import android.annotation.SuppressLint
import android.app.*
import android.app.Notification.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException

class CallListenerService : Service() {

    private var LISTEN: Boolean = false
    private val LISTENER_PORT = 50003 // You can change this port number as needed
    private val BUF_SIZE = 1024 // Adjust buffer size as needed
    private val LOG_TAG = "CallListenerService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        startCallListener()
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType", "NewApi")
    private fun startForeground() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "call_listener_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Call Listener Service", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = Builder(this, channelId)
            .setContentTitle("Call Listener Service")
            .setContentText("Listening for incoming calls")
            .setSmallIcon(R.drawable.notification) // Change this to your notification icon
        val notification = notificationBuilder.build()
        startForeground(1, notification)
    }

    // Call listener function
    private fun startCallListener() {
        // Creates the listener thread
        LISTEN = true
        val listener = Thread {
            try {
                // Set up the socket and packet to receive
                Log.i(LOG_TAG, "Incoming call listener started")
                val socket: DatagramSocket = DatagramSocket(LISTENER_PORT)
                socket.soTimeout = 1000
                val buffer = ByteArray(BUF_SIZE)
                val packet = DatagramPacket(buffer, BUF_SIZE)
                while (LISTEN) {
                    // Listen for incoming call requests
                    try {
                        Log.i(LOG_TAG, "Listening for incoming calls")
                        socket.receive(packet)
                        val data = String(buffer, 0, packet.length)
                        Log.i(
                            LOG_TAG,
                            "Packet received from " + packet.address + " with contents: " + data
                        )
                        val action = data.substring(0, 4)
                        if (action == "CAL:") {
                            // Received a call request. Start the ReceiveCallActivity
                            val address = packet.address.toString()
                            val name = data.substring(4, packet.length)
                            val intent = Intent(
                                this,
                                ReceiveCallActivity::class.java
                            )
                            intent.putExtra("EXTRA_CONTACT", name)
                            intent.putExtra("EXTRA_IP", address.substring(1, address.length))
                            // IN_CALL = true // You might want to handle IN_CALL state elsewhere
                            // LISTEN = false;
                            // stopCallListener();
                            startActivity(intent)
                        } else {
                            // Received an invalid request
                            Log.w(
                                LOG_TAG,
                                packet.address.toString() + " sent invalid message: " + data
                            )
                        }
                    } catch (e: Exception) {
                        // Handle exception
                    }
                }
                Log.i(LOG_TAG, "Call Listener ending")
                socket.disconnect()
                socket.close()
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException in listener $e")
            }
        }
        listener.start()
    }

    // Function to stop call listener
    private fun stopCallListener() {
        // Ends the listener thread
        LISTEN = false
    }

    override fun onDestroy() {
        stopCallListener()
        super.onDestroy()
    }
}
