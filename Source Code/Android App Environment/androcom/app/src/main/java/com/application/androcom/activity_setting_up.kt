package com.application.androcom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.ContextCompat

import android.text.Editable
import com.google.gson.Gson
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

class activity_setting_up : AppCompatActivity() {

    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY="user_data"
    private val gson = Gson()

    data class User(val firstName: String, val lastName: String):Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_up)

        val firstNameInput: EditText = findViewById(R.id.first_name_input)
        val lastNameInput: EditText = findViewById(R.id.last_name_input)
        val myButton: Button = findViewById(R.id.my_button)


        // Load the user's name from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        var user: User? = null

        if (userData != null) {
            // User name is already stored, you may want to handle it (e.g., pre-fill the EditTexts)
            user = gson.fromJson(userData, User::class.java)
            firstNameInput.setText(user?.firstName)
            lastNameInput.setText(user?.lastName)
        }

        // Add TextWatcher to observe changes in first name input
        firstNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                updateButtonState(firstNameInput.text.isNotEmpty(), lastNameInput.text.isNotEmpty(), myButton)
            }
        })

        // Add TextWatcher to observe changes in last name input
        lastNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                updateButtonState(firstNameInput.text.isNotEmpty(), lastNameInput.text.isNotEmpty(), myButton)
            }
        })

        // Add click listener to the button for navigation
        myButton.setOnClickListener {
            // Check if both first name and last name are filled
            val isFirstNameFilled = firstNameInput.text.isNotEmpty()
            val isLastNameFilled = lastNameInput.text.isNotEmpty()

            if (isFirstNameFilled && isLastNameFilled) {
                val firstName = firstNameInput.text.toString()
                val lastName = lastNameInput.text.toString()
                val user = User(firstName, lastName)  //  User object

                saveUserDataToPrefs(user)  // Pass the User object
                val userJson = gson.toJson(user)


            //  Intent to start the HomeScreen
                val intent = Intent(this@activity_setting_up, activity_HomeScreen::class.java)

                intent.putExtra("userJson", userJson)

                startActivity(intent)

                // Finish the current activity (activity_setting_up)
                finish()
            } else {
                // Fields are not filled, display a message or handle it accordingly
                Toast.makeText(this@activity_setting_up, "Please fill out both fields", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun updateButtonState(isFirstNameFilled: Boolean, isLastNameFilled: Boolean, button: Button) {
        if (isFirstNameFilled && isLastNameFilled) {

            button.setBackgroundResource(R.drawable.new_button_background)
            button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
        } else {
            // Revert to the original button color or styling
            button.setBackgroundResource(R.drawable.continuebutton)

        }
    }
    private fun saveUserDataToPrefs(user: User) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        // Serialize the user data to JSON
        val userDataJson = gson.toJson(user)
        editor.putString(USER_DATA_KEY, userDataJson)
        editor.apply()
    }
}
