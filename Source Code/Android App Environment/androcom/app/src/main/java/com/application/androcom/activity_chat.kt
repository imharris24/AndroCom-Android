package com.application.androcom

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// socket programming dependencies
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket

class activity_chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var inputMessage: EditText
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // set layout to activity_chat
        setContentView(R.layout.activity_chat)

        // start listening for messages
        startMessageReceiver()

        val textname = findViewById<TextView>(R.id.textname)
        val backicon = findViewById<AppCompatImageView>(R.id.backicon)
        recyclerView = findViewById(R.id.chatRecyler)
        progressBar = findViewById(R.id.progress)
        inputMessage = findViewById(R.id.inputmessage)
        val sendButton = findViewById<AppCompatImageView>(R.id.sendbutton)
        adapter = ChatAdapter()
        recyclerView.adapter = adapter

        val receivedIP = intent.getStringExtra("ip")

        // load messages from database
        loadMessagesForIp(receivedIP)

        // event listener for contact profile
        textname.setOnClickListener {
            val intent = Intent(this, activity_UserProfile::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for send button
        sendButton.setOnClickListener {
            var message = inputMessage.text.toString()
            val receivedIP = intent.getStringExtra("ip")

            // send message
            Send_Message("$receivedIP", message)

            // reset input to null
            inputMessage.setText("")
        }

        val username = intent.getStringExtra("username")
        if (username != null) {
            textname.text = username
        }

        // event listener for back
        backicon.setOnClickListener{
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        chatRecyclerView = findViewById(R.id.chatRecyler)
    }

    // function that loads messages from database
    private fun loadMessagesForIp(ip: String?): List<Message> {

        val messages = mutableListOf<Message>()
        val dbHelper = ChatDatabaseHelper(this)

        // fetch messages from SQLite database
        val chatMessages = dbHelper.getAllChatsForIp(ip)

        // convert to message objects
        chatMessages.forEach { chatMessage ->
            val text = chatMessage.message
            val isSent = chatMessage.isSent
            val timestamp = chatMessage.timestamp
            val message = Message(text, isSent)
            messages.add(message)

            // add messages on UI
            this.runOnUiThread {
                adapter.addMessage(message)
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }

        return messages
    }

    // function to send message
    @RequiresApi(Build.VERSION_CODES.O)
    fun Send_Message(IP_Address: String, Message: String) {
        Thread {
            try {

                val senderIp = LocalIpAddressProvider().getLocalIpAddress()

                // create an instance of encryption
                val aes = AESEncryption()

                // encrypt message
                val encryptedMessage = aes.encrypt("$senderIp: $Message", "AndroCom")

                // socket programming
                val My_Socket = Socket(IP_Address, SocketPorts.MESSAGE_PORT)
                val writer = OutputStreamWriter(My_Socket.getOutputStream())
                writer.write(encryptedMessage)
                writer.flush()
                My_Socket.close()

                // save message to SQLite database
                val sentMessage = Message(Message, true)
                saveMessage(sentMessage, senderIp, IP_Address, isSent = true)

                // push messages to UI
                this.runOnUiThread {
                    adapter.addMessage(sentMessage)
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: IOException) {
                Log.e("Exception", "error sending message: $e")
            }
        }.start()
    }

    // function that receives the message from message socket
    @RequiresApi(Build.VERSION_CODES.O)
    fun startMessageReceiver() {

        // create instance for encryption method
        val aes = AESEncryption()

        Thread {
            try {
                while (true) {

                    // socket programming
                    val serverSocket = ServerSocket(SocketPorts.MESSAGE_PORT)
                    val clientSocket = serverSocket.accept()
                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    var message = reader.readLine()

                    // decrypt message
                    message = aes.decrypt(message, "AndroCom")

                    val parts = message.split(": ", limit = 2)
                    val senderIp = parts[0]
                    val splitMessage = parts[1]

                    val receivedMessage = Message(splitMessage, false)

                    // save message to SQLite database
                    saveMessage(receivedMessage, senderIp, LocalIpAddressProvider().getLocalIpAddress(), isSent = false)

                    // push message to UI
                    runOnUiThread {
                        this.adapter.addMessage(receivedMessage)
                        this.recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                    serverSocket.close()
                }
            } catch (e: IOException) {
                Log.e("Exception", "error receiving message: $e")
            }
        }.start()
    }

    // function to save message to SQLite database
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMessage(message: Message, senderIp: String, receiverIp: String?, isSent: Boolean) {

        val dbHelper = ChatDatabaseHelper(this)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val isSaved = dbHelper.addChat(sender = senderIp, receiver = receiverIp, message = message.text, timestamp = timestamp, isSent = isSent)

        // if message is saved in database
        if (isSaved) {
            Log.d("saveMessage", "Data saved successfully")
        } else {
            Log.d("saveMessage", "Failed to save data")
        }
    }






}