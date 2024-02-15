package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.application.androcom.databinding.ActivityHomeScreenBinding

class activity_HomeScreen : AppCompatActivity() {
    private lateinit var binding : ActivityHomeScreenBinding
    private lateinit var userIPArray: ArrayList<UserIP>
    private lateinit var listView: ListView
    private lateinit var adapter : RecentChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        // set layout to activity_home_screen
        setContentView(binding.root)

        val callsIcon = findViewById<ImageView>(R.id.callsIcon)
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        val callText = findViewById<TextView>(R.id.callText)
        val settingText = findViewById<TextView>(R.id.settingText)
        val wifi = findViewById<ImageView>(R.id.userlist)

        // event listener for active user button
        wifi.setOnClickListener{
             Intent(this, activity_Contacts::class.java).apply {
                 startActivity(this)
                 finish()
            }
        }

        // event listener for calls icon
        callsIcon.setOnClickListener {
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
            finish()
        }
        callText.setOnClickListener {
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
            finish()
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

        // create DB instance
        val dbHelper = ChatDatabaseHelper(this)

        // get IP addresses of all users within the database
        userIPArray = dbHelper.getAllUserIPs("${LocalIpAddressProvider().getLocalIpAddress()}")

        listView = findViewById(R.id.recentchat)
        adapter = RecentChatAdapter(this,userIPArray)
        listView.adapter = adapter

        // event listener for chat item
        listView.setOnItemClickListener {_,_, position, _ ->
            val selectedUserIP = userIPArray[position]
            val username = selectedUserIP.name
            val ip = selectedUserIP.IP

            startChatActivity(username, ip)
        }
    }

    // function to start chat activity
    private fun startChatActivity(username: String, ip: String) {
        val intent = Intent(this, activity_chat::class.java)
        intent.putExtra("username", username)
        intent.putExtra("ip", ip)
        startActivity(intent)
        finish()
    }
}
