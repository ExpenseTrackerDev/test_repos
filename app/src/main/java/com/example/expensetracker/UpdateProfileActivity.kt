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
import androidx.core.view.isVisible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var username: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnEdit: Button
    private lateinit var btnBack: ImageView

    private lateinit var userId: String

    private var isEditMode = false   // Track edit mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_update_profile)

        username = findViewById(R.id.chooseusername)
        email = findViewById(R.id.enteremail)
        phone = findViewById(R.id.enterphone)
        btnUpdate = findViewById(R.id.verify)
        btnEdit = findViewById(R.id.editBtn)
        btnBack = findViewById(R.id.back_btn)

        // Disable editing initially
        toggleEditing(false)

        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""

        loadUserProfile()

        btnEdit.setOnClickListener {
            toggleEditing(true)
        }

        btnUpdate.setOnClickListener {
            updateProfile(prefs)
        }

        btnBack.setOnClickListener {
            handleBackPressed()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun handleBackPressed() {
        if (isEditMode) {
            // Exit edit mode instead of leaving
            toggleEditing(false)
            loadUserProfile()
            Toast.makeText(this, "Edit cancelled", Toast.LENGTH_SHORT).show()
        } else {
            // Leave activity normally
            startActivity(Intent(this, settingsActivity::class.java))
            finish()
        }
    }

    private fun loadUserProfile() {
        RetrofitClient.instance.getProfile(userId)
            .enqueue(object : Callback<ProfileResponse> {
                override fun onResponse(
                    call: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { user ->
                            username.setText(user.username ?: "")
                            email.setText(user.email ?: "")
                            phone.setText(user.phone ?: "")
                        }
                    } else {
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun toggleEditing(enable: Boolean) {
        isEditMode = enable
        username.isEnabled = enable
        email.isEnabled = enable
        phone.isEnabled = enable

        btnUpdate.isVisible = enable
        btnUpdate.isEnabled = enable

        btnEdit.isVisible = !enable
    }

    private fun updateProfile(prefs: android.content.SharedPreferences) {

        val updatedUsername = username.text.toString().trim()
        val updatedEmail = email.text.toString().trim()
        val updatedPhone = phone.text.toString().trim()

        val updateMap = mutableMapOf<String, String>()

        if (updatedUsername.isNotEmpty()) updateMap["username"] = updatedUsername
        if (updatedEmail.isNotEmpty()) updateMap["email"] = updatedEmail
        if (updatedPhone.isNotEmpty()) updateMap["phone"] = updatedPhone

        if (updateMap.isEmpty()) {
            Toast.makeText(this, "No changes to update", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.updateProfile(userId, updateMap)
            .enqueue(object : Callback<ApiResponse> {
                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            response.body()?.message ?: "Updated!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Go back to view mode
                        toggleEditing(false)

                        // Save locally
                        prefs.edit().apply {
                            if (updateMap.containsKey("username")) putString("username", updatedUsername)
                            if (updateMap.containsKey("email")) putString("email", updatedEmail)
                            if (updateMap.containsKey("phone")) putString("phone", updatedPhone)
                            apply()
                        }

                    } else {
                        Toast.makeText(
                            this@UpdateProfileActivity,
                            "Update failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Toast.makeText(
                        this@UpdateProfileActivity,
                        "Error: ${t.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
