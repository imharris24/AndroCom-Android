package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.application.androcom.databinding.ActivityHomeScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class activity_HomeScreen : AppCompatActivity() {
    private lateinit var recentChatsJob: Job
    private lateinit var binding : ActivityHomeScreenBinding
    private lateinit var userIPArray: ArrayList<UserIP>
    private lateinit var listView: ListView
    private lateinit var adapter : RecentChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        // set layout to activity_home_screen
        setContentView(binding.root)


        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        val settingText = findViewById<TextView>(R.id.settingText)
        val wifi = findViewById<ImageView>(R.id.userlist)

        // event listener for active user button
        wifi.setOnClickListener{
             Intent(this, activity_Contacts::class.java).apply {
                 startActivity(this)
                 finish()
            }
        }

        // event listener for settings icon
        settingText.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }
        settingsIcon.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }


        recentChatsJob = CoroutineScope(Dispatchers.Main).launch {
            Log.i("recentChatsJob","Start Coroutine")
            while (true) {
                updateRecentChats()
                delay(100) // Delay for 1 seconds
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the coroutine job when the activity is destroyed
        recentChatsJob.cancel()
    }


    private fun updateRecentChats() {
        val dbHelper = ChatDatabaseHelper(this)
        userIPArray = dbHelper.getAllUserIPs(LocalIpAddressProvider().getLocalIpAddress())

//        userIPArray.add(UserIP("Umer Ahmed","192.168.100.52","Mac 1"))
//        userIPArray.add(UserIP("Muhammad Harris","192.168.100.28","Mac 2"))

        listView = findViewById(R.id.recentchat)
        adapter = RecentChatAdapter(this, userIPArray)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedUserIP = userIPArray[position]
            val username = selectedUserIP.name
            val ip = selectedUserIP.IP
            val mac = selectedUserIP.mac
            startChatActivity(username, ip, mac)
        }
    }

    private fun startChatActivity(username: String, ip: String, mac: String) {
        val intent = Intent(this, activity_chat::class.java)
        intent.putExtra("username", username)
        intent.putExtra("ip", ip)
        intent.putExtra("mac", mac)
        startActivity(intent)
        finish()
    }


}
