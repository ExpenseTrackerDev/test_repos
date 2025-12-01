package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class loginActivity : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnCreate: TextView
    private lateinit var btnForget: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.username)
        etPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.loginbtn)
        btnForget = findViewById(R.id.forgetpass)
        btnCreate = findViewById(R.id.signup)


        btnLogin.setOnClickListener {
            val identifier = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (identifier.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(identifier, pass)

            RetrofitClient.instance.loginUser(request).enqueue(object : retrofit2.Callback<LoginResponse> {
                override fun onResponse(call: retrofit2.Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val loginData = response.body()!!

                        // Save user info in SharedPreferences
                        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
                        prefs.edit().apply {
                            putString("userId", loginData.userId)
                            putString("username", loginData.username)
                            putString("email", loginData.email)
                            apply()
                        }

                        // Start dashboard
                        val intent = Intent(this@loginActivity, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()

                        Toast.makeText(this@loginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@loginActivity, "Invalid Email/Username or Password", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@loginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }




        btnForget.setOnClickListener {
            val intent = Intent(this, resetActivity::class.java)
            intent.putExtra("source","login")
            startActivity(intent)
            finish()
        }
        btnCreate.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
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


