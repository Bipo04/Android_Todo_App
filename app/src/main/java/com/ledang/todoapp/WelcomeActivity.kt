package com.ledang.todoapp

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ledang.todoapp.data.UserPreferences

class WelcomeActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var btnContinue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        etName = findViewById(R.id.et_name)
        btnContinue = findViewById(R.id.btn_continue)

        btnContinue.setOnClickListener {
            val name = etName.text.toString().trim()
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save user name and mark first launch complete
            UserPreferences.saveUserName(this, name)
            UserPreferences.setFirstLaunchComplete(this)

            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
