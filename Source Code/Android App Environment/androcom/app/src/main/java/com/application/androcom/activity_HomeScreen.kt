package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.application.androcom.databinding.ActivityHomeScreenBinding
import java.net.DatagramSocket

class activity_HomeScreen : AppCompatActivity() {
    private lateinit var binding : ActivityHomeScreenBinding

    private lateinit var userIPArray: ArrayList<UserIP>
    private lateinit var listView: ListView
    private lateinit var adapter : RecentChatAdapter

    private lateinit var socket: DatagramSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val callsIcon = findViewById<ImageView>(R.id.callsIcon)
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        val callText=findViewById<TextView>(R.id.callText)
        val settingText=findViewById<TextView>(R.id.settingText)
        val wifi=findViewById<ImageView>(R.id.userlist)

        wifi.setOnClickListener{
             Intent(this, activity_Contacts::class.java).apply {
                startActivity(this)
            }
        }
        callsIcon.setOnClickListener {

            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
        }
        callText.setOnClickListener {
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
        }
        settingText.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
        }
        settingsIcon.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
        }

        val dbHelper = ChatDatabaseHelper(this)
        userIPArray = dbHelper.getAllUserIPs("${LocalIpAddressProvider().getLocalIpAddress()}")
//        // queryt mac aur ip
//
//        userIPArray.add(UserIP("Wasiyahh", "192.168.0.1"))
//        userIPArray.add(UserIP("Harris", "192.168.0.2"))
//        userIPArray.add(UserIP("Umer", "192.168.0.3"))
        listView = findViewById(R.id.recentchat)
        adapter = RecentChatAdapter(this,userIPArray)
        listView.adapter=adapter

        listView.setOnItemClickListener {_,_, position, _ ->
            val selectedUserIP = userIPArray[position]
            val username = selectedUserIP.name
            val ip = selectedUserIP.IP

            startChatActivity(username, ip)
        }

        //  startIPReceiver()
    }




/* private fun startIPReceiver() {
     CoroutineScope(Dispatchers.IO).launch {
         try {
             // Using Static SERVER_PORT From  SocketPorts Class
             socket = DatagramSocket(SocketPorts.SERVER_PORT)
             socket.reuseAddress = true;
             while (true) {
                 try {
                     val packet = DatagramPacket(ByteArray(1024), 1024)
                     socket.receive(packet)

                     val receivedData = String(packet.data, 0, packet.length)

                     try {
                         // Assuming JSON format:
                         val devices = JSONObject(receivedData)
                         val newUserIPs = ArrayList<UserIP>()
                         val devicesJSONObject = devices.getJSONObject("devices")
                         val deviceKeys = devicesJSONObject.keys()

                         for (ipKey in deviceKeys) {
                             val ip = devicesJSONObject.getString(ipKey)
                             newUserIPs.add(UserIP("$ipKey","$ip"))
                         }


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
 private fun closeSocket() {
     if (::socket.isInitialized && !socket.isClosed) {
         socket.close()
     }
 }
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
 }*/
private fun startChatActivity(username: String, ip: String) {
    val intent = Intent(this, activity_chat::class.java)
    intent.putExtra("username", username)
    intent.putExtra("ip", ip)
    startActivity(intent)
}
    }
