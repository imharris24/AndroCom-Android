package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class activity_help : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set layout to activity_help
        setContentView(R.layout.activity_help)

        val back_icon = findViewById<ImageView>(R.id.back_icon)
        val licensce_button = findViewById<Button>(R.id.license_button)

        // event listener for back icon
        back_icon.setOnClickListener {
            val intent = Intent(this, activity_app_settings::class.java)
            startActivity(intent)
            finish()
        }

        // event listener for licence button
        licensce_button.setOnClickListener {
            val intent = Intent(this, activity_license::class.java)
            startActivity(intent)
            finish()
        }
    }
}