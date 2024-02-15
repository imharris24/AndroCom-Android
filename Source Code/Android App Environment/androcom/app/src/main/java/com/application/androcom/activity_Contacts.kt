package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.application.androcom.databinding.ActivityContactsBinding
import org.json.JSONObject
import java.io.IOException
// socket dependencies
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.DatagramPacket
import java.net.DatagramSocket

class activity_Contacts : AppCompatActivity() {
    private lateinit var binding : ActivityContactsBinding
    private lateinit var userIPArray: ArrayList<UserIP>
    private lateinit var listView: ListView
    private lateinit var adapter : IPAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<ImageView>(R.id.callsIcon)
        findViewById<ImageView>(R.id.settingsIcon)
        findViewById<TextView>(R.id.callText)
        findViewById<TextView>(R.id.settingText)
        findViewById<TextView>(R.id.chatText)
        findViewById<ImageView>(R.id.chatIcon)

        // event listener for call icon
        binding.callsIcon.setOnClickListener {
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
            finish()
        }
        binding.callText.setOnClickListener {
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for settings icon
        binding.settingText.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }
        binding.settingsIcon.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for chats icon
        binding.chatIcon.setOnClickListener {
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.chatText.setOnClickListener {
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        userIPArray = ArrayList()

        listView = findViewById(R.id.contactlist)
        adapter = IPAdapter(this,userIPArray)
        listView.adapter=adapter

        // event listener for active item
        listView.setOnItemClickListener {_,_, position, _ ->
            val selectedUserIP = userIPArray[position]
            val username = selectedUserIP.name
            val ip = selectedUserIP.IP
            startChatActivity(username, ip)
        }

        // get information from server
        startIPReceiver()
    }

    // function to receive information from server
    private fun startIPReceiver() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // start datagram socket
                val socket = DatagramSocket(null).apply{
                    reuseAddress = true
                    bind(InetSocketAddress(SocketPorts.SERVER_PORT))
                }

                // get local ip
                val ipAddress = LocalIpAddressProvider().getLocalIpAddress()

                while (true) {
                    try {
                        val packet = DatagramPacket(ByteArray(1024), 1024)
                        socket.receive(packet)

                        val receivedData = String(packet.data, 0, packet.length)

                        try {

                            val devices = JSONObject(receivedData)
                            val newUserIPs = ArrayList<UserIP>()
                            val devicesJSONObject = devices.getJSONObject("devices")
                            val deviceKeys = devicesJSONObject.keys()

                            for (ipKey in deviceKeys) {
                                val ip = devicesJSONObject.getString(ipKey)
                                if(ipAddress == ip){
                                    continue
                                }
                                newUserIPs.add(UserIP("$ipKey","$ip"))
                            }

                            // push active users to UI
                            runOnUiThread {
                                userIPArray.clear()
                                userIPArray.addAll(newUserIPs)
                                adapter.notifyDataSetChanged()
                            }
                        } catch (e: JSONException) {
                            handleInvalidDataError(e)
                        }
                    } catch (e: IOException) {
                        handleNetworkError(e)
                    }
                }
            } catch (e: SocketException) {
                handleSocketError(e)
            }
        }
    }

    // functions to display socket errors if any
    private fun handleInvalidDataError(e: JSONException) {
        runOnUiThread {
            Toast.makeText(this, "Error parsing received data: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun handleNetworkError(e: IOException) {
        runOnUiThread {
            Toast.makeText(this, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun handleSocketError(e: SocketException) {
        runOnUiThread {
            Toast.makeText(this, "Socket error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // function to navigate to chat activity
    private fun startChatActivity(username: String, ip: String) {
        val intent = Intent(this, activity_chat::class.java)
        intent.putExtra("username", username)
        intent.putExtra("ip", ip)
        startActivity(intent)
        finish()
    }
}