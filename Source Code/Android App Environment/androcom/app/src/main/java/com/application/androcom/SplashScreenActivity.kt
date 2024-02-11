package com.application.androcom

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.google.gson.Gson
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()

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
                // Check if the user profile exists
                if (userProfileExists()) {
                    val user = loadUserFromPrefs()
                    navigateToHomeScreen(user)
                } else {
                    // If the profile doesn't exist, navigate to MainScreen
                    val mainScreenIntent = Intent(this@SplashScreenActivity, MainScreen::class.java)
                    startActivity(mainScreenIntent)
                }
                finish()
            }
        }
    }
    private fun userProfileExists(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
        return sharedPreferences.contains(USER_DATA_KEY)
    }

    private fun loadUserFromPrefs(): activity_setting_up.User {
        val sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
        val userDataJson = sharedPreferences.getString(USER_DATA_KEY, null)
        return gson.fromJson(userDataJson, activity_setting_up.User::class.java)
    }

    private fun navigateToHomeScreen(user: activity_setting_up.User) {
        val homeIntent = Intent(this@SplashScreenActivity, activity_HomeScreen::class.java)
        homeIntent.putExtra("user", user)
        startActivity(homeIntent)
    }
}