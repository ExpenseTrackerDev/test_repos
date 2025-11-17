package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create)

        val backCreate = findViewById<ImageView>(R.id.back_btn)
        val createAccountBtn = findViewById<Button>(R.id.createacnt)
        val signinBtn = findViewById<TextView>(R.id.signin)
        val username = findViewById<EditText>(R.id.chooseusername)
        val email = findViewById<EditText>(R.id.enteremail)
        val phone = findViewById<EditText>(R.id.enterphone)
        val password = findViewById<EditText>(R.id.createpass)
        val confirmPassword = findViewById<EditText>(R.id.confirm_pass)

        // 🔹 Back button → go to LoginActivity
        backCreate.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
            finish()
        }
        signinBtn.setOnClickListener {
            val intent = Intent(this, loginActivity::class.java)
            startActivity(intent)
            finish()
        }



//        createAccountBtn.setOnClickListener {
//            val user = username.text.toString().trim()
//            val mail = email.text.toString().trim()
//            val phn = phone.text.toString().trim()
//            val pass = password.text.toString()
//            val confirmPass = confirmPassword.text.toString()
//
//            if (user.isEmpty() || mail.isEmpty() || phn.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
//                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (pass != confirmPass) {
//                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // If validation passes, go to verification page
//            val intent = Intent(this, VerificationActivity::class.java)
//            intent.putExtra("username", user)
//            intent.putExtra("email", mail)
//            intent.putExtra("phone", phn)
//            intent.putExtra("password", pass)
//            intent.putExtra("source", "create")
//            startActivity(intent)
//            finish()
//        }
        createAccountBtn.setOnClickListener {
            val user = username.text.toString().trim()
            val mail = email.text.toString().trim()
            val phn = phone.text.toString().trim()
            val pass = password.text.toString()
            val confirmPass = confirmPassword.text.toString()

            if (user.isEmpty() || mail.isEmpty() || phn.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call backend
            val request = RegisterRequest(user, mail, phn, pass, confirmPass)
            RetrofitClient.instance.registerUser(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@CreateActivity, response.body()?.message ?: "OTP sent", Toast.LENGTH_SHORT).show()
                        // Move to VerificationActivity
                        val intent = Intent(this@CreateActivity, VerificationActivity::class.java)
                        intent.putExtra("email", mail)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@CreateActivity, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@CreateActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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