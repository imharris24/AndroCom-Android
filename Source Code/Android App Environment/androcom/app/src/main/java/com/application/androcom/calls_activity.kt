package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class calls_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // set layout to activity_calls
        setContentView(R.layout.activity_calls)

        val chatIcon = findViewById<ImageView>(R.id.chatIcon)
        val settingsIcon = findViewById<ImageView>(R.id.settingsIcon)
        val settingText=findViewById<TextView>(R.id.settingText)
        val chatText=findViewById<TextView>(R.id.chatText)

        // event listeners for chat
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

        // event listener to settings
        settingText.setOnClickListener {
            val userName = intent.getStringExtra("user_name")
            val intent = Intent(this, activity_app_settings::class.java)
            intent.putExtra("user_name", userName)
            startActivity(intent)
            finish()
        }
        settingsIcon.setOnClickListener {
            val userName = intent.getStringExtra("user_name")
            val intent = Intent(this, activity_app_settings::class.java)
            intent.putExtra("user_name", userName)
            startActivity(intent)
            finish()
        }
    }
}