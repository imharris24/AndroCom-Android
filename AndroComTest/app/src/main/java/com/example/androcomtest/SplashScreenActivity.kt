package com.example.androcomtest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.example.androcomtest.R
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val ivNote: ImageView = findViewById(R.id.logo)

        // Fade in animation
        ivNote.alpha = 0f
        ivNote.animate().apply {
            duration = 1500
            alpha(1f)
            withEndAction {
                val intent = Intent(this@SplashScreenActivity, MainActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}
