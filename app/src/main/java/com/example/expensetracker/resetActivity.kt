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

class resetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset)

        val btnback=findViewById<ImageView>(R.id.back_btn)
        val btnReset=findViewById<Button>(R.id.resetpass)
        val password = findViewById<EditText>(R.id.newpass)
        val confirmPassword = findViewById<EditText>(R.id.confirmpass)

        btnback.setOnClickListener {
//            val intent = Intent(this, loginActivity::class.java)
//            startActivity(intent)
            finish()
        }
        btnReset.setOnClickListener {
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // If validation passes, go to verification page
            val intent = Intent(this, VerificationActivity::class.java)
            intent.putExtra("password", pass)
            startActivity(intent)
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}