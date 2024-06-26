package com.application.androcom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Socket
import java.net.SocketException


class activity_chat : AppCompatActivity() {

    val LOG_TAG = "UDPchat"
    private val LISTENER_PORT = 50003
    private val BUF_SIZE = 1024
    private val displayName: String? = null
    private val STARTED = false
    private var IN_CALL = false
    private var LISTEN = false

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var inputMessage: EditText
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView

    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // set layout to activity_chat
        setContentView(R.layout.activity_chat)

        // issue here
        if (!ChatDatabaseHelper(this).isIPBlocked(intent.getStringExtra("ip").toString())) {
            Log.d("not blocked"," startCallListener()")
            startCallListener()
        }else{
            Log.d("blocked"," startCallListener()")
        }


        val textname = findViewById<TextView>(R.id.textname)
        val videoButton = findViewById<AppCompatImageView>(R.id.video)
        val backicon = findViewById<AppCompatImageView>(R.id.backicon)
        recyclerView = findViewById(R.id.chatRecyler)
        progressBar = findViewById(R.id.progress)
        inputMessage = findViewById(R.id.inputmessage)
        val sendButton = findViewById<AppCompatImageView>(R.id.sendbutton)
        adapter = ChatAdapter()
        recyclerView.adapter = adapter


        val receivedIP = intent.getStringExtra("ip")
        val receivedMac = "${intent.getStringExtra("mac")}"
        val receivedName = "${intent.getStringExtra("username")}"
        Log.i("Test", "Name: ${receivedName}\n IP: ${receivedIP}\n MAC: $receivedMac");

        videoButton.setOnClickListener {
            val intent = Intent(this, ActivityVideoCall::class.java)
            intent.putExtra("name", receivedName)
            intent.putExtra("ip", receivedIP)
            startActivity(intent)
        }

        // audio call functionality
        val callButton = findViewById<AppCompatImageView>(R.id.imageaudio)
        callButton.setOnClickListener{
            if (!ChatDatabaseHelper(this).isIPBlocked(intent.getStringExtra("ip").toString())) {
                // Collect details about the selected contact
                val contact = receivedName
                val ip = receivedIP
                IN_CALL = true

                // Send this information to the MakeCallActivity and start that activity
                val intent = Intent(this, MakeCallActivity::class.java)
                intent.putExtra("EXTRA_CONTACT", contact)
                intent.putExtra("EXTRA_IP", ip);
                intent.putExtra("EXTRA_DISPLAYNAME", contact);
                startActivity(intent);
            }
        }


// Start the coroutine to load messages for the given IP every 3 seconds
        coroutineScope.runLoadMessagesCoroutine(receivedIP)

        // event listener for contact profile
        textname.setOnClickListener {
            val intent = Intent(this, activity_UserProfile::class.java)

            intent.putExtra("username", receivedName)
            intent.putExtra("ip", receivedIP)
            intent.putExtra("mac", receivedMac)
            startActivity(intent)

        }

        // event listener for send button
        sendButton.setOnClickListener {
            var message = inputMessage.text.toString()
            val receivedIP = intent.getStringExtra("ip")
            if(message.isNotEmpty()) {
                // send message
                if (receivedIP != null) {
                    Log.i("Bug", "Message is Sent: ${message}")
                    if (!ChatDatabaseHelper(this).isIPBlocked(receivedIP)){
                            Send_Message(Name=receivedName,IP_Address=receivedIP,Mac=receivedMac,Message= message)
                    }

                }
                // reset input to null
                inputMessage.setText("")
            }else{
                runOnUiThread {
                    Toast.makeText(this, "Enter Message Before Sending", Toast.LENGTH_LONG).show()
                }
            }

        }

        val username = intent.getStringExtra("username")
        if (username != null) {
            textname.text = username
        }
        intent.putExtra("myuser", textname.text.toString())

        // event listener for back
        backicon.setOnClickListener{
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        chatRecyclerView = findViewById(R.id.chatRecyler)
    }
    // Make sure to cancel the coroutine when it's no longer needed (e.g., in onDestroy of the activity)
    override fun onDestroy() {
        super.onDestroy()
        Log.i("onDestroy","coroutineScope canceled")
        coroutineScope.cancel()
    }





    // Define a coroutine function to load messages for an IP address
    private suspend fun loadMessagesForIpCoroutine(ip: String?): List<Message> {
        val messages = mutableListOf<Message>()
        val dbHelper = ChatDatabaseHelper(this@activity_chat) // Assuming 'this' refers to the current context or activity

        // Fetch messages from SQLite database for the given IP address
        val chatMessages = dbHelper.getAllChatsForIp(ip)
//        Log.d("loadMessagesForIp", chatMessages.toString())

        adapter.clearMessages()
        // Convert ChatMessage objects to Message objects
        chatMessages.forEach { chatMessage ->
            val text = chatMessage.message
            val isSent = chatMessage.isSent
            val timestamp = chatMessage.timestamp
            val message = Message(text, isSent)

            messages.add(message)

            // Update UI to display the received message
            withContext(Dispatchers.Main) {

                adapter.addMessage(message)
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }

        // Log information about loaded messages
        if (messages.isNotEmpty()) {
//            Log.d("MessageLoading", "${messages.size} messages loaded for IP: $ip")
        } else {
//            Log.d("MessageLoading", "No messages loaded for IP: $ip ")
        }

        return messages
    }

    // Coroutine function to run loadMessagesForIpCoroutine every 3 seconds
    private fun CoroutineScope.runLoadMessagesCoroutine(ip: String?) = launch {
        while (true) {
            loadMessagesForIpCoroutine(ip)
            delay(150)
        }
    }


    // function to send message
    @RequiresApi(Build.VERSION_CODES.O)
    fun Send_Message(Name: String,IP_Address: String,Mac: String, Message: String) {
        // load user information from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        val user: activity_setting_up.User?
        var name: String = ""
        if (userData != null) {
            user = gson.fromJson(userData, activity_setting_up.User::class.java)
            name = "${user.firstName} ${user.lastName}"
        }

        // adjust mac Address
        Thread {
            try {
                val senderIp = LocalIpAddressProvider().getLocalIpAddress()
                // create an instance of encryption
                val aes = AESEncryption()
                // encrypt message
                val encryptedMessage = aes.encrypt("{$senderIp:$name:${LocalIpAddressProvider().getLocalIpAddress()}}: $Message", "AndroCom")

                // socket programming
                val sendSocket = Socket(IP_Address, SocketPorts.MESSAGE_PORT)
                val writer = OutputStreamWriter(sendSocket.getOutputStream())
                writer.write(encryptedMessage)
                writer.flush()
                sendSocket.close()

                // save message to SQLite database
                val sentMessage = Message(Message, true)
                saveMessage(message=sentMessage, receiverIp= IP_Address, name=Name,mac=Mac,isSent = true)

            } catch (e: IOException) {
                Log.e("Exception", "error sending message: $e")
            }
        }.start()

    }


    // function to save message to SQLite database
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMessage(message: Message ,receiverIp: String, isSent: Boolean,name: String,mac:String) {

        val dbHelper = ChatDatabaseHelper(this)
        val isSaved = dbHelper.addChat(receiver = receiverIp, message = message.text, name = name,mac=mac, isSent = isSent)

        // if message is saved in database
        if (isSaved) {
            Log.d("saveMessage", "Data saved successfully")
        } else {
            Log.d("saveMessage", "Failed to save data")
        }
    }

    // issue here
    // call functions
    fun startCallListener() {
        if (!ChatDatabaseHelper(this).isIPBlocked(intent.getStringExtra("ip").toString())) {
        val receivedName = "${intent.getStringExtra("username")}"
        // Creates the listener thread
        LISTEN = true
        val listener = Thread {
            try {
                // Set up the socket and packet to receive
                Log.i("LOG_TAG", "Incoming call listener started")
                val socket: DatagramSocket = DatagramSocket(LISTENER_PORT)
                socket.soTimeout = 1000
                val buffer = ByteArray(BUF_SIZE)
                val packet = DatagramPacket(buffer, BUF_SIZE)
                while (LISTEN) {
                    // Listen for incoming call requests
                    try {
                        Log.i(
                            LOG_TAG,
                            "Listening for incoming calls"
                        )
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
                            intent.putExtra("DisplayName", receivedName)
                            intent.putExtra("EXTRA_IP", address.substring(1, address.length))
                            IN_CALL = true
                            //LISTEN = false;
                            //stopCallListener();
                            startActivity(intent)
                        } else {
                            // Received an invalid request
                            Log.w(
                                LOG_TAG,
                                packet.address.toString() + " sent invalid message: " + data
                            )
                        }
                    } catch (e: Exception) {
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
    }

    private fun stopCallListener() {
        // Ends the listener thread
        LISTEN = false
    }


}