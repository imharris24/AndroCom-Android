package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class activity_license : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set layout to activity_license
        setContentView(R.layout.activity_license)

        val back_icon = findViewById<ImageView>(R.id.back_icon)
        // event listener for back icon
        back_icon.setOnClickListener {
            val intent = Intent(this, activity_help::class.java)
            startActivity(intent)
            finish()
        }
    }
}