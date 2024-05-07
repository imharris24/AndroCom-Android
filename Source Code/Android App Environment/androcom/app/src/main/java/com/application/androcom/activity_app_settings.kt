package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson

class activity_app_settings : AppCompatActivity() {

    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // set layout to activity_app_settings
        setContentView(R.layout.activity_app_settings)

        val dispname = findViewById<TextView>(R.id.dispname)
        val dispip = findViewById<TextView>(R.id.dispip)
        val chatIcon = findViewById<ImageView>(R.id.chatIcon)
        val chatText = findViewById<TextView>(R.id.chatText)
        val keysIcon = findViewById<ImageView>(R.id.keys)
        val accountText = findViewById<TextView>(R.id.account)
        val messageIcon = findViewById<ImageView>(R.id.messageicon)
        val blockuser = findViewById<TextView>(R.id.blockedUsers)
        val blockedipdescription=findViewById<TextView>(R.id.blocked_description)
        val notificationIcon = findViewById<ImageView>(R.id.notification)
        val notificationText = findViewById<TextView>(R.id.texts)
        val helpIcon = findViewById<ImageView>(R.id.help)
        val helpText = findViewById<TextView>(R.id.helptext)

        // load user information from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        val user: activity_setting_up.User?

        // if user exists
        if (userData != null) {
            user = gson.fromJson(userData, activity_setting_up.User::class.java)
            dispname.text = "${user.firstName} ${user.lastName}"
        }

        // display local ip address
        val address_provider = LocalIpAddressProvider()
        dispip.text = address_provider.getLocalIpAddress()

        // event listener for chat
        chatIcon.setOnClickListener {
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        chatText.setOnClickListener {
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for account settings
        keysIcon.setOnClickListener {
            //navigateToMainActivity()
        }
        accountText.setOnClickListener {
            //navigateToMainActivity()
        }

        // event listener for traffic prioritization
        messageIcon.setOnClickListener {
            val intent = Intent(this, BlockedIPS::class.java)
            startActivity(intent)
            finish()
        }
        blockuser.setOnClickListener {
            val intent = Intent(this, BlockedIPS::class.java)
            startActivity(intent)
            finish()
        }
        blockedipdescription.setOnClickListener {
            val intent = Intent(this, BlockedIPS::class.java)
            startActivity(intent)
            finish()
        }
        // event listener for notifications
        notificationIcon.setOnClickListener {
            //navigateToMainActivity()
        }
        notificationText.setOnClickListener {
            //navigateToMainActivity()
        }

        // event listener for help
        helpIcon.setOnClickListener {
            val intent = Intent(this, activity_help::class.java)
            startActivity(intent)
            finish()
        }
        helpText.setOnClickListener {
            val intent = Intent(this, activity_help::class.java)
            startActivity(intent)
            finish()
        }
    }
}