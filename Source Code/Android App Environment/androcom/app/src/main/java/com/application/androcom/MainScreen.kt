package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set layout to activity_main_screen
        setContentView(R.layout.activity_main_screen)

        val setupButton: android.widget.Button = findViewById(R.id.my_button)

        setupButton.setOnClickListener {
            // navigate to activity_setting_up
            val intent = Intent(this@MainScreen, activity_setting_up::class.java)
            startActivity(intent)
            finish()
        }
    }
}
