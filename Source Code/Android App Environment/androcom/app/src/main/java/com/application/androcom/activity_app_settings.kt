package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
//import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson

class activity_app_settings : AppCompatActivity() {
    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_settings)
        val dispname = findViewById<TextView>(R.id.dispname)
        val chatIcon = findViewById<ImageView>(R.id.chatIcon)
        val chatText=findViewById<TextView>(R.id.chatText)
        val callIcon = findViewById<ImageView>(R.id.callsIcon)
        val callText=findViewById<TextView>(R.id.callText)
        val keysIcon = findViewById<ImageView>(R.id.keys)
        val accountText = findViewById<TextView>(R.id.account)
        val messageIcon=findViewById<ImageView>(R.id.messageicon)
        val trafficText=findViewById<TextView>(R.id.traffic)
        val notificationIcon=findViewById<ImageView>(R.id.notification)
        val notificationText=findViewById<TextView>(R.id.texts)
        val helpIcon=findViewById<ImageView>(R.id.help)
        val helpText=findViewById<TextView>(R.id.helptext)

        // Load the user's name from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        val user: activity_setting_up.User?

        if (userData != null) {
            user = gson.fromJson(userData, activity_setting_up.User::class.java)
            dispname.text = "${user.firstName} ${user.lastName}"
        }

        chatIcon.setOnClickListener {
            // Handle the click on the call icon
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
        }
        chatText.setOnClickListener {
            // Handle the click on the call icon
            val intent = Intent(this, activity_HomeScreen::class.java)
            startActivity(intent)
        }
        callIcon.setOnClickListener {
            // Handle the click on the call icon
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
        }
        callText.setOnClickListener {
            // Handle the click on the call icon
            val intent = Intent(this, calls_activity::class.java)
            startActivity(intent)
        }

        keysIcon.setOnClickListener {
            // Handle the click on the keys icon
            navigateToMainActivity()
        }

        accountText.setOnClickListener {
            // Handle the click on the account text
            navigateToMainActivity()
        }
        messageIcon.setOnClickListener {
            // Handle the click on the keys icon
            navigateToMainActivity()
        }

        trafficText.setOnClickListener {
            // Handle the click on the account text
            navigateToMainActivity()
        }
        notificationIcon.setOnClickListener {
            // Handle the click on the keys icon
            navigateToMainActivity()
        }

        notificationText.setOnClickListener {
            // Handle the click on the account text
            navigateToMainActivity()
        }
        helpIcon.setOnClickListener {
            // Handle the click on the keys icon
            navigateToMainActivity()
        }

        helpText.setOnClickListener {
            // Handle the click on the account text
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Optional: finish the current activity if you don't want to go back to it
    }
    }
