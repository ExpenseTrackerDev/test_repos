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

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)

        // Now safely access your views
        val otp = findViewById<EditText>(R.id.otp)
        val btnBack = findViewById<ImageView>(R.id.back_btn)
        val btnVerify = findViewById<Button>(R.id.verify)

        // Back button
        btnBack.setOnClickListener {
            finish() // simply go back to previous screen
        }

        // Verify button
        btnVerify.setOnClickListener {
            val OTP = otp.text.toString().trim()

            if (OTP.isEmpty()) {
                Toast.makeText(this, "Please fill the field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // After verifying OTP, go to login screen
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
