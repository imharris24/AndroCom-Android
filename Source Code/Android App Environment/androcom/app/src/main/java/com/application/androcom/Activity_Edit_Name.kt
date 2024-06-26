package com.application.androcom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.gson.Gson

class Activity_Edit_Name : AppCompatActivity() {
    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY = "user_data"
    private val gson = Gson()

    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private var user: activity_setting_up.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_name)
        val backIcon=findViewById<ImageView>(R.id.backButton)
        val firstname=findViewById<EditText>(R.id.first_name_input)
        val lastname=findViewById<EditText>(R.id.last_name_input)
        val SaveChanges=findViewById<android.widget.Button>(R.id.SaveChanges)
        backIcon.setOnClickListener{
            backIcon.setOnClickListener {
                val intent = Intent(this, activity_app_settings::class.java)
                startActivity(intent)
                finish()
            }

        }
        // load user information from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)


        // if user exists
        if (userData != null) {
            user = gson.fromJson(userData, activity_setting_up.User::class.java)
            firstname.setText(user!!.firstName)
            lastname.setText(user!!.lastName)
        }

        SaveChanges.setOnClickListener {
            // Get the new first and last name from EditText fields
            val newFirstName = firstname.text.toString()
            val newLastName = lastname.text.toString()
            // Update user object if it's not null
            user?.apply {
                firstName = newFirstName
                lastName = newLastName

                // Save the updated user object to SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString(USER_DATA_KEY, gson.toJson(this))
                editor.apply()

                // Display a toast to indicate successful save
                Toast.makeText(this@Activity_Edit_Name, "Changes saved successfully", Toast.LENGTH_SHORT).show()

                // Restart the app
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }


    }
}