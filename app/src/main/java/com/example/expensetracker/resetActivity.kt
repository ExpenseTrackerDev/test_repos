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
        val email = findViewById<EditText>(R.id.emailReset)
        val source = intent.getStringExtra("source")


        btnback.setOnClickListener {
            when (source) {
                "login" -> {
                    val intent = Intent(this, loginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                "settings" -> {
                    val intent = Intent(this, settingsActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> finish() // fallback
            }
        }
        btnReset.setOnClickListener {
            val emailText = email.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (emailText.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ResetRequest(emailText, pass, confirmPass)

            RetrofitClient.instance.resetRequest(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@resetActivity, "OTP sent to email", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@resetActivity, VerificationActivity::class.java)
                        intent.putExtra("email", emailText)
                        intent.putExtra("source", "reset")
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@resetActivity, response.body()?.message ?: "Failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@resetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}