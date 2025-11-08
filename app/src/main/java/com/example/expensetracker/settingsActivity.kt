package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class settingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Must call setContentView first
        setContentView(R.layout.activity_settings)
        enableEdgeToEdge()

        // Edge to edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Views
        val btnBack = findViewById<ImageView>(R.id.back_btn)
        val btnUpProfile = findViewById<TextView>(R.id.profileup)
        val btnChangePass = findViewById<TextView>(R.id.passchange)

        // Back button click
        btnBack.setOnClickListener {
            val intent = Intent(this, dashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Update profile click
        btnUpProfile.setOnClickListener {
            val intent = Intent(this, UpdateProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Change password click
        btnChangePass.setOnClickListener {
            val intent = Intent(this, resetActivity::class.java)
            intent.putExtra("source","settings")
            startActivity(intent)
            finish()
        }
    }
}
