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

    // read files to access user data
    private val PREFS_FILENAME = "com.application.androcom.prefs"
    private val USER_DATA_KEY="user_data"
    private val gson = Gson()

    // user information
    data class User(val firstName: String, val lastName: String):Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set layout to activity_setting_up
        setContentView(R.layout.activity_setting_up)

        val firstNameInput: EditText = findViewById(R.id.first_name_input)
        val lastNameInput: EditText = findViewById(R.id.last_name_input)
        val myButton: Button = findViewById(R.id.my_button)

        // load username from SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val userData = sharedPreferences.getString(USER_DATA_KEY, null)
        var user: User? = null

        // if user exists
        if (userData != null) {
            user = gson.fromJson(userData, User::class.java)

            // set text on UI
            firstNameInput.setText(user?.firstName)
            lastNameInput.setText(user?.lastName)
        }

        // TextWatcher: observes changes in text
        firstNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            // function that updates the state of button once input is filled
            override fun afterTextChanged(editable: Editable?) {
                updateButtonState(firstNameInput.text.isNotEmpty(), lastNameInput.text.isNotEmpty(), myButton)
            }
        })

        lastNameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

            // function that updates the state of button once input is filled
            override fun afterTextChanged(editable: Editable?) {
                updateButtonState(firstNameInput.text.isNotEmpty(), lastNameInput.text.isNotEmpty(), myButton)
            }
        })

        // listener for continue button
        myButton.setOnClickListener {

            val isFirstNameFilled = firstNameInput.text.isNotEmpty()
            val isLastNameFilled = lastNameInput.text.isNotEmpty()

            // if input is filled
            if (isFirstNameFilled && isLastNameFilled) {

                val firstName = firstNameInput.text.toString()
                val lastName = lastNameInput.text.toString()
                val user = User(firstName, lastName)

                // save user name
                saveUserDataToPrefs(user)
                val userJson = gson.toJson(user)


                val intent = Intent(this@activity_setting_up,SplashScreenActivity::class.java)
                startActivity(intent)
                finish()
                // route to home screen
//                val intent = Intent(this@activity_setting_up, activity_HomeScreen::class.java)
//                intent.putExtra("userJson", userJson)
//                startActivity(intent)
//                finish()

            }
            // do nothing if button is disabled
            else {
                Toast.makeText(this@activity_setting_up, "Cannot continue without user's name.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // function to update button state
    private fun updateButtonState(isFirstNameFilled: Boolean, isLastNameFilled: Boolean, button: Button) {
        if (isFirstNameFilled && isLastNameFilled) {
            button.setBackgroundResource(R.drawable.new_button_background)
            button.setTextColor(ContextCompat.getColor(button.context, R.color.white))
        } else {
            button.setBackgroundResource(R.drawable.continuebutton)
        }
    }

    // function to save username
    private fun saveUserDataToPrefs(user: User) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        val userDataJson = gson.toJson(user)
        editor.putString(USER_DATA_KEY, userDataJson)
        editor.apply()
    }
}
