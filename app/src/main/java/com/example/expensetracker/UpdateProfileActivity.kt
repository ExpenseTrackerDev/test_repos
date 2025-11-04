package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UpdateProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_profile)

        // Now safely access all views
        val btnBack = findViewById<ImageView>(R.id.back_btn)
        val btnUpProfile = findViewById<Button>(R.id.verify)
        val username = findViewById<EditText>(R.id.chooseusername)
        val email = findViewById<EditText>(R.id.enteremail)
        val phone = findViewById<EditText>(R.id.enterphone)

        // Update button click
        btnUpProfile.setOnClickListener {
            val user = username.text.toString().trim()
            val mail = email.text.toString().trim()
            val phn = phone.text.toString().trim()

            if (user.isEmpty() || mail.isEmpty() || phn.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Save updated info to DB or shared preferences here
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

            // Go back to settings screen
            val intent = Intent(this, settingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Back button click
        btnBack.setOnClickListener {
            val intent = Intent(this, settingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Edge-to-edge system insets handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
