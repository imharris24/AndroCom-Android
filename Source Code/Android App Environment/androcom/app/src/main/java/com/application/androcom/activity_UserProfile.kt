package com.application.androcom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class activity_UserProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // set layout to activity_user_profile
        setContentView(R.layout.activity_user_profile)

        val receivedIP = intent.getStringExtra("ip")
        val UserName = findViewById<TextView>(R.id.username)
        val BackIcon = findViewById<ImageView>(R.id.backicon)
        val ChatIcon = findViewById<ImageView>(R.id.messaging)
        val blockip = findViewById<TextView>(R.id.blockuser)
        val MacAdress = findViewById<TextView>(R.id.usermac)
        UserName.text = receivedIP
        MacAdress.text = receivedIP
        blockip.text = receivedIP

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
            finish()
        }
    }
}