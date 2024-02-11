package com.application.androcom

//import android.os.AsyncTask
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
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class activity_chat : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var inputMessage: EditText
    private lateinit var adapter: ChatAdapter
    private lateinit var chatRecyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

       startMessageReceiver()


        val aes = AESEncryption()
        val textname =findViewById<TextView>(R.id.textname)
        val backicon=findViewById<AppCompatImageView>(R.id.backicon)
        recyclerView = findViewById(R.id.chatRecyler)

        progressBar = findViewById(R.id.progress)
        inputMessage = findViewById(R.id.inputmessage)
        val sendButton = findViewById<AppCompatImageView>(R.id.sendbutton)
        adapter = ChatAdapter()
        recyclerView.adapter = adapter
        val receivedIP = intent.getStringExtra("ip")
        loadMessagesForIp(receivedIP)
        textname.setOnClickListener {
            val intent = Intent(this, activity_UserProfile::class.java)
            startActivity(intent)
        }
        // Click on Send Button
        sendButton.setOnClickListener {
            var message = inputMessage.text.toString()
            val receivedIP = intent.getStringExtra("ip")
            Send_Message("$receivedIP", message)
            inputMessage.setText("")
        }


        val username = intent.getStringExtra("username")
        if (username != null) {
            textname.text = username

        }
        backicon.setOnClickListener{
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
        }
        chatRecyclerView = findViewById(R.id.chatRecyler)
    }



    private fun loadMessagesForIp(ip: String?): List<Message> {
        val messages = mutableListOf<Message>()
        val dbHelper = ChatDatabaseHelper(this) // Assuming 'this' refers to the current context or activity

        // Fetch messages from SQLite database for the given IP address
        val chatMessages = dbHelper.getAllChatsForIp(ip)

        // Convert ChatMessage objects to Message objects
        chatMessages.forEach { chatMessage ->
            val text = chatMessage.message
            val isSent = chatMessage.isSent
            val timestamp = chatMessage.timestamp
            val message = Message(text, isSent)
            Log.d("Display: line-122", text)
            messages.add(message)
            // Update UI to display the received message
            this.runOnUiThread {
                adapter.addMessage(message)
                recyclerView.scrollToPosition(adapter.itemCount - 1)
            }
        }

        // Log information about loaded messages
        if (messages.isNotEmpty()) {
            Log.d("MessageLoading", "${messages.size} messages loaded for IP: $ip")
        } else {
            Log.d("MessageLoading", "No messages loaded for IP: $ip")
        }



        return messages
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun Send_Message(IP_Address: String, Message: String) {
        Thread {
            try {

               val senderIp = LocalIpAddressProvider().getLocalIpAddress()
                val aes = AESEncryption()
                val encryptedMessage = aes.encrypt("$senderIp: $Message", "AndroCom")
                Log.i("TAG", "IP: $IP_Address, $Message")
                val My_Socket = Socket(IP_Address, SocketPorts.MESSAGE_PORT)

                val writer = OutputStreamWriter(My_Socket.getOutputStream())
                writer.write(encryptedMessage)
                writer.flush()

                Log.i("TAG", "Message Sent")
                My_Socket.close()



                val sentMessage = Message(Message, true)
                saveMessage(sentMessage, senderIp, IP_Address, isSent = true)


                this.runOnUiThread {

                    adapter.addMessage(sentMessage)
                    recyclerView.scrollToPosition(adapter.itemCount - 1)
                }
            } catch (e: IOException) {
                // Handle error
                Log.e("TAG", "Error sending message: $e")
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startMessageReceiver() {
        val aes = AESEncryption()
        Thread {
            try {
                while (true) {
                    val serverSocket = ServerSocket(SocketPorts.MESSAGE_PORT)
                    val clientSocket = serverSocket.accept()
                    val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                    var message = reader.readLine()
                    message = aes.decrypt(message, "AndroCom")
                    // Split the concatenated message into sender's IP address and message content
                    val parts = message.split(": ", limit = 2)
                    val senderIp = parts[0]
                    val splitMessage = parts[1]

                    val receivedMessage = Message(splitMessage, false)
                    saveMessage(receivedMessage, senderIp, LocalIpAddressProvider().getLocalIpAddress(), isSent = false)

//                     Update UI to display the received message
                    runOnUiThread {
                        Log.e("Receiver", "Message is displayed")
                        this.adapter.addMessage(receivedMessage)
                        this.recyclerView.scrollToPosition(adapter.itemCount - 1)
                    }
                    serverSocket.close()
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Error receiving message: $e")
            }
        }.start()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMessage(message: Message, senderIp: String, receiverIp: String?, isSent: Boolean) {
        val dbHelper = ChatDatabaseHelper(this)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val isSaved = dbHelper.addChat(sender = senderIp, receiver = receiverIp, message = message.text, timestamp = timestamp, isSent = isSent)

        // Check if data was saved in SQLite
        if (isSaved) {
            // Data saved successfully
            Log.d("saveMessage", "Data saved successfully")
        } else {
            // Failed to save data
            Log.d("saveMessage", "Failed to save data")
        }
    }






}