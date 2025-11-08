package com.example.expensetracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ReportActivity : AppCompatActivity() {

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnDownload: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)
        recyclerView = findViewById(R.id.recyclerViewReport)
        btnDownload = findViewById(R.id.btnDownloadExcel)

        val backBtn: ImageView = findViewById(R.id.back_btn)
        backBtn.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this)

        setupSpinners()
        loadTransactions()

        btnDownload.setOnClickListener {
            if (checkPermission()) {
                downloadReportFromBackend()
            } else {
                requestPermission()
            }
        }
    }

    private fun setupSpinners() {
        // Months
        val months = (1..12).map { it.toString() }
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)

        // Years (e.g., 2023 to current year)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val years = (2020..currentYear).map { it.toString() }
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
    }

    private fun loadTransactions() {
        // TODO: Fetch transactions from backend or local DB and set to RecyclerView
        // For now, you can set a dummy adapter
        val dummyData = listOf(
            Transaction("2025-11-01", "Income", 1000.0, "Salary", "Monthly salary"),
            Transaction("2025-11-02", "Expense", 200.0, "Food", "Lunch")
        )
        recyclerView.adapter = TransactionAdapter(dummyData)
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadReportFromBackend()
        } else {
            Toast.makeText(this, "Storage permission required to download report.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadReportFromBackend() {
        val month = spinnerMonth.selectedItemPosition + 1
        val year = spinnerYear.selectedItem.toString()

        val url = "http://YOUR_SERVER_IP:3000/download-report?month=$month&year=$year"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ReportActivity, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@ReportActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val fileName = "ExpenseReport_${System.currentTimeMillis()}.csv"
                val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)

                try {
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }

                    runOnUiThread {
                        Toast.makeText(this@ReportActivity, "Report downloaded:\n${file.absolutePath}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@ReportActivity, "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

