package com.application.androcom

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView

class activity_UserProfile : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val receivedIP = intent.getStringExtra("ip")
        val receivedMac = intent.getStringExtra("mac")
        val receivedUSER = intent.getStringExtra("username")

        Log.d("activity_UserProfile", "IP: $receivedIP, MAC: $receivedMac, USER: $receivedUSER")

        val IpAdress = findViewById<TextView>(R.id.userip)
        val UserName = findViewById<TextView>(R.id.username)
        val BackIcon = findViewById<ImageView>(R.id.backicon)
        val BlockIcon = findViewById<ImageView>(R.id.blockIcon)
        val BlockIconText = findViewById<TextView>(R.id.blockIconText)
        val blockip = findViewById<TextView>(R.id.blockuser)
        val MacAdress = findViewById<TextView>(R.id.usermac)

        UserName.text = receivedUSER ?: "No Username"
        MacAdress.text = receivedMac ?: "No MAC Address"
        blockip.text = " " ?: "No IP"
        IpAdress.text = receivedIP ?: "No IP"

        val switchMuteNotification = findViewById<Switch>(R.id.mutenotification)
// Get the initial state from the database
        val chatDatabaseHelper = ChatDatabaseHelper(this)
        val isMuted = chatDatabaseHelper.isIPMuted(receivedIP.toString())
        switchMuteNotification.isChecked = isMuted
        Log.d("Switch", if (isMuted) "On" else "Off")

// Set a listener for the switch state change
        switchMuteNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                chatDatabaseHelper.addIPToMute(receivedIP.toString())
                Log.d("Switch", "Muted")
            } else {
                chatDatabaseHelper.removeIPFromMute(receivedIP.toString())
                Log.d("Switch", "Unmuted")
            }
        }


        BackIcon.setOnClickListener {

            finish()
        }

        BlockIcon.setOnClickListener {
            blockIP(receivedIP, receivedUSER)
        }

        BlockIconText.setOnClickListener {
            blockIP(receivedIP, receivedUSER)
        }
    }

    private fun blockIP(ipAddress: String?, username: String?) {
        if (ipAddress != null) {
            Log.d("blocked", "IP address blocked: $ipAddress")
            ChatDatabaseHelper(this).addIPToBlock(ipAddress, username.toString())
        }
    }
}
