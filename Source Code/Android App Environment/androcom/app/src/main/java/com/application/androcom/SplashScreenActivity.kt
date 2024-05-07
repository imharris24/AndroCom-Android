package com.application.androcom

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

class SplashScreenActivity : AppCompatActivity() {
    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()
    private lateinit var dialog: AlertDialog


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set view to splash screen
        setContentView(R.layout.activity_splash_screen)
        val ivNote: ImageView = findViewById(R.id.logo)

        // load user information from SharedPreferences
        val sharedPreferences: SharedPreferences =
            getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        val user: activity_setting_up.User? =
            gson.fromJson(userData, activity_setting_up.User::class.java)


        requestPermission()
        // Start the MessageForegroundService
        startService(Intent(this, MessageForegroundService::class.java))
//        startService( Intent(this, CallListenerService::class.java))


        if (userProfileExists()) {
//           Send TCP PACKET TP SERVER TO SEND IF WE ARE CONNECTED AD_HOC NETWORK
//            sendTcpPacket(
//                "192.168.1.1",
//                SocketPorts.NETWORK_CHECK_PORT,
//                "${user?.firstName} ${user?.lastName}: ${LocalIpAddressProvider().getLocalIpAddress()}"
//            )
        }


        // animation
        ivNote.alpha = 0f
        ivNote.animate().apply {
            duration = 5000
            alpha(1f)
            withEndAction {
                // if the user exist then navigate to home screen
                if (userProfileExists()) {
                    val user = loadUserFromPrefs()
                    navigateToHomeScreen(user)
                }
                // if the user does not exits then goto MainScreen for user setup
                else {
                    val mainScreenIntent = Intent(this@SplashScreenActivity, MainScreen::class.java)
                    startActivity(mainScreenIntent)
                }

                finish()
            }
        }
    }


    // function that tell whether the user profile exists in file or not
    private fun userProfileExists(): Boolean {
        val sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
        return sharedPreferences.contains(USER_DATA_KEY)
    }

    // function that loads user data from file
    private fun loadUserFromPrefs(): activity_setting_up.User {
        val sharedPreferences = getSharedPreferences(PREFS_FILENAME, MODE_PRIVATE)
        val userDataJson = sharedPreferences.getString(USER_DATA_KEY, null)
        return gson.fromJson(userDataJson, activity_setting_up.User::class.java)
    }

    // function that routes to HomeScreen
    private fun navigateToHomeScreen(user: activity_setting_up.User) {
        val homeIntent = Intent(this@SplashScreenActivity, activity_HomeScreen::class.java)
        homeIntent.putExtra("user", user)
        startActivity(homeIntent)
    }

    // This Send UserName And Ip Address To Server And Check if User is Connected To Ad-Hoc Network or Not
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun sendTcpPacket(serverAddress: String, port: Int, message: String) {
        Log.e("SplashScreenActivity", "sendTcpPacket")
        // Use CoroutineScope to launch a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create socket and output stream
                val socket = Socket()
                val socketAddress = InetSocketAddress(serverAddress, port)
                socket.connect(socketAddress, 3000) // Timeout in milliseconds
                val outputStream = socket.outputStream

                // Convert message to UTF-8 bytes and send
                val messageBytes = message.toByteArray(Charsets.UTF_8)
                outputStream.write(messageBytes)
                outputStream.flush()

                // Create input stream
                val inputStream = socket.inputStream

                // Read server response bytes
                val responseBytes = inputStream.readBytes()

                // Convert response to string and check if it's ACK
                val response = String(responseBytes, Charsets.UTF_8).trim().uppercase()

                // IF RESPONSE == "ACK" THEN USER CAN USE ANDRO-COM APP ELSE CLOSE THE APP
                if (response == "ACK") {
                    Log.e("TCP", "Received ACK from server: $response")
                    notificationHandle(message) // Show notification
                } else {
                    Log.e("TCP", "Received Not ACK from server: $response")
                    closeApp() // Handle errors gracefully and close app if necessary
                }

                // Close resources in correct order
                inputStream.close()
                outputStream.close()
                socket.close()
            } catch (e: SocketTimeoutException) {
                Log.e("TCP", "Connection timeout")
                closeApp() // Handle errors gracefully and close app if necessary
            } catch (e: Exception) {
                closeApp() // Handle errors gracefully and close app if necessary
            }
        }
    }


    // THIS FUNCTION WILL DISPLAY ALERT AND CLOSE APP
    private fun closeApp() {
        Log.i("Splashscreen", "closeApp")
        val customLayout = layoutInflater.inflate(R.layout.custom_alert, null)
        runOnUiThread {
            Log.i("Splashscreen", "Inside runOnUiThread")
            val builder = AlertDialog.Builder(this)
            builder.setView(customLayout)
            dialog = builder.create()
            dialog.show()
            Thread {
                Log.i("Splashscreen", "Starting thread")
                Thread.sleep(3000) // Adjusted duration to 3 seconds
                runOnUiThread {
                    Log.i("Splashscreen", "Inside thread's runOnUiThread")
                    dialog.dismiss()
                    // The line below will forcefully terminate the application
                    System.exit(0)
                }
            }.start()
        }
    }

    // THIS FUNCTION WILL DISPLAY NOTIFICATIONS ON SUCCESSFUL CONNECTION WITH SERVER
    private fun notificationHandle(message: String) {
        val notificationHelper =
            NotificationHelper(this, "AndroCom", "AndroCom", R.drawable.notification)
        notificationHelper.showNotification(
            title = message,
            message = "Your connected with our Ad-Hoc Network. \nYour IP Address is  ${LocalIpAddressProvider().getLocalIpAddress()}"
        )
    }


    private fun hasForegroundPermission() =
        ActivityCompat.checkSelfPermission(this,
            Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    private fun hasRecordAudio() =
        ActivityCompat.checkSelfPermission(this,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(){
        Log.d("PermissionRequest","Request for Permission")
        var permissionToRequest = mutableListOf<String>()
        if(!hasForegroundPermission()){
            permissionToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if(!hasRecordAudio()){
            permissionToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if(permissionToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this,permissionToRequest.toTypedArray(),0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.isNotEmpty()){
            for(i in grantResults.indices){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.d("PermissionRequest","${permissions[i]} granted.")
                }
            }
        }
    }
}

