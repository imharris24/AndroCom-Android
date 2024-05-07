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
        // set layout to activity_user_profile
        setContentView(R.layout.activity_user_profile)

        val receivedIP = intent.getStringExtra("ip")
        val receivedUSER = intent.getStringExtra("username")
        val receivedMac = intent.getStringExtra("mac")
        val IpAdress=findViewById<TextView>(R.id.userip)
        val UserName = findViewById<TextView>(R.id.username)
        val BackIcon = findViewById<ImageView>(R.id.backicon)
        val ChatIcon = findViewById<ImageView>(R.id.messaging)
        val BlockIcon =findViewById<ImageView>(R.id.blockIcon)
        val BlockIconText = findViewById<TextView>(R.id.blockIconText)
        val blockip = findViewById<TextView>(R.id.blockuser)
        val MacAdress = findViewById<TextView>(R.id.usermac)
        UserName.text = receivedUSER
        MacAdress.text = receivedMac
        blockip.text = receivedIP
        IpAdress.text=receivedIP

        // Find the Switch in your activity or fragment
        val switchMuteNotification= findViewById<Switch>(R.id.mutenotification)

        // setting state of switch on load
        if (ChatDatabaseHelper(this).addIPToMute(receivedIP.toString())) {
            // if user is already mute turn switch on
            switchMuteNotification.isChecked = true
            Log.d("Switch","On")
        } else {
            // else switch off
            switchMuteNotification.isChecked =false
            Log.d("Switch","Off")
        }

        // Listening of change in state
        switchMuteNotification.setOnCheckedChangeListener { _, isChecked ->
            // Check if the Switch is checked (on), already mute
            if (isChecked) {
                // remove for db
                ChatDatabaseHelper(this).removeIPFromMute(receivedIP.toString())
                // reset state of switch
                switchMuteNotification.isChecked =false
            }
            else {
                // If the Switch is not checked (off), remove the IP address from mute
                // add to db
                ChatDatabaseHelper(this).addIPToMute(receivedIP.toString())
                // reset state of switch
                switchMuteNotification.isChecked =false
            }
        }

        // event listener for chat icon
        ChatIcon.setOnClickListener {
            val intent = Intent(this, activity_chat::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for back icon
        BackIcon.setOnClickListener {
            val intent = Intent(this, activity_chat::class.java)
            startActivity(intent)

        }

        BlockIcon.setOnClickListener {
            blockIP(receivedIP,receivedUSER)
        }

        BlockIconText.setOnClickListener {
            blockIP(receivedIP,receivedUSER)
        }
    }

    private fun blockIP(ipAddress: String?, username: String?) {
        if (ipAddress != null) {
            Log.d("blocked", "IP address blocked: $ipAddress")
            // user to block user table
            ChatDatabaseHelper(this).addIPToBlock(ipAddress, username.toString())
//            val intent = Intent(this, BlockedIPS::class.java)
//            intent.putStringArrayListExtra("blocked_ips", arrayListOf(ipAddress))
//            intent.putStringArrayListExtra("Blocked_Username", arrayListOf(username))
//            startActivity(intent)
        }
    }

}
