package com.app.androcom

import android.os.Bundle
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // call splash screen
        setContentView(R.layout.activity_splash_screen)
    }
}
