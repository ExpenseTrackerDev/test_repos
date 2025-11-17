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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text


class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verification)

        // Now safely access your views
        val otp = findViewById<EditText>(R.id.otp)
        val btnBack = findViewById<ImageView>(R.id.back_btn)
        val btnVerify = findViewById<Button>(R.id.verify)
        val resendBtn = findViewById<TextView>(R.id.resendOtp)
        val source = intent.getStringExtra("source") // "reset" or "create"
        val maskedEmail = findViewById<TextView>(R.id.emailMasked)

        val email = intent.getStringExtra("email") ?: ""
        val masked = maskEmail(email)
        maskedEmail.text = "$masked"


        // Back button
        btnBack.setOnClickListener {
            when (source) {
                "reset" -> {
                    val intent = Intent(this, resetActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                "create" -> {
                    val intent = Intent(this, CreateActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else -> finish() // fallback
            }
        }

        // Verify button
//        btnVerify.setOnClickListener {
//            val OTP = otp.text.toString().trim()
//
//            if (OTP.isEmpty()) {
//                Toast.makeText(this, "Please fill the field", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            // After verifying OTP, go to login screen
//            val intent = Intent(this, loginActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
        btnVerify.setOnClickListener {
            val OTP = otp.text.toString().trim()
            val email = intent.getStringExtra("email") ?: return@setOnClickListener
            val source = intent.getStringExtra("source") ?: "create"

            if (OTP.isEmpty()) {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(source == "reset"){
                val request = ResetVerifyRequest(email, OTP)
                RetrofitClient.instance.resetVerify(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                    override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerificationActivity, response.body()?.message ?: "Password updated", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@VerificationActivity, loginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@VerificationActivity, response.body()?.message ?: "OTP verification for reset password failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@VerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // Normal verification (create flow)
                val request = VerifyRequest(email, OTP)
                RetrofitClient.instance.verifyEmail(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                    override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@VerificationActivity, response.body()?.message ?: "Verified", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@VerificationActivity, loginActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@VerificationActivity, "OTP verification for create account failed", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                        Toast.makeText(this@VerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        resendBtn.setOnClickListener {
            val email = intent.getStringExtra("email") ?: return@setOnClickListener
            val request = ResendOtpRequest(email)

            RetrofitClient.instance.resendOtp(request).enqueue(object : retrofit2.Callback<ApiResponse> {
                override fun onResponse(call: retrofit2.Call<ApiResponse>, response: retrofit2.Response<ApiResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@VerificationActivity, response.body()?.message ?: "OTP sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@VerificationActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(this@VerificationActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

        }




        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email

        val name = parts[0]
        val domain = parts[1]

        val maskedName = when {
            name.length <= 2 -> name.first() + "*"
            else -> name.substring(0, 3) + "***"
        }

        return "$maskedName@$domain"
    }
}
