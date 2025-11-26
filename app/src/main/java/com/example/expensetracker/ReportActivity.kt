package com.example.expensetracker

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.ExpenseResponse
import com.example.expensetracker.IncomeResponse
import com.example.expensetracker.model.Transaction
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ReportActivity : AppCompatActivity() {

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnDownload: Button
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()

    private val apiService = RetrofitClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)
        recyclerView = findViewById(R.id.recyclerViewReport)
        btnDownload = findViewById(R.id.btnDownloadExcel)

        recyclerView.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(transactions)
        recyclerView.adapter = transactionAdapter

        findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        setupSpinners()
        loadTransactions() // initial load

        spinnerMonth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                loadTransactions()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnDownload.setOnClickListener {
            if (checkPermission()) {
                downloadReportFromBackend()
            } else {
                requestPermission()
            }
        }
    }

    private fun setupSpinners() {
        val months = (1..12).map { it.toString() }
        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (2020..currentYear).map { it.toString() }
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)

        val cal = Calendar.getInstance()
        spinnerMonth.setSelection(cal.get(Calendar.MONTH))
        val yearIndex = years.indexOf(cal.get(Calendar.YEAR).toString())
        if (yearIndex >= 0) spinnerYear.setSelection(yearIndex)
    }

    private fun getUserId(): String? {
        val sharedPref = getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
        return sharedPref.getString("userId", null)
    }

    private fun loadTransactions() {
        val userId = getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val month = spinnerMonth.selectedItemPosition + 1
        val year = spinnerYear.selectedItem.toString().toInt()

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Clear previous transactions
        transactions.clear()

        // Helper function to filter by month/year
        fun <T> filterByMonthYear(list: List<T>, getDate: (T) -> String?): List<T> {
            return list.filter {
                val dateStr = getDate(it)
                if (dateStr != null) {
                    val cal = Calendar.getInstance()
                    cal.time = sdf.parse(dateStr) ?: Date()
                    (cal.get(Calendar.MONTH) + 1) == month && cal.get(Calendar.YEAR) == year
                } else false
            }
        }

        var expensesLoaded = false
        var incomesLoaded = false
        var filteredExpenses: List<ExpenseResponse> = emptyList()
        var filteredIncomes: List<IncomeResponse> = emptyList()

        // Combine transactions only once both API calls finish
        fun updateTransactions() {
            if (!expensesLoaded || !incomesLoaded) return

            transactions.clear()
            transactions.addAll(filteredExpenses.map {
                Transaction(
                    type = "Expense",
                    amount = it.amount,
                    category = it.category,
                    date = it.date,
                    description = it.description
                )
            })

            transactions.addAll(filteredIncomes.map {
                Transaction(
                    type = "Income",
                    amount = it.amount,
                    category = it.category,
                    date = it.date,
                    description = it.description
                )
            })

            // Sort descending by date
            transactions.sortByDescending { sdf.parse(it.date) }

            // Update adapter
            transactionAdapter.updateData(transactions)
        }

        // Fetch expenses
        apiService.getExpenses(userId).enqueue(object : Callback<List<ExpenseResponse>> {
            override fun onResponse(call: Call<List<ExpenseResponse>>, response: Response<List<ExpenseResponse>>) {
                if (response.isSuccessful) filteredExpenses = filterByMonthYear(response.body() ?: emptyList()) { it.date }
                expensesLoaded = true
                updateTransactions()
            }

            override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                expensesLoaded = true
                Toast.makeText(this@ReportActivity, "Failed to load expenses: ${t.message}", Toast.LENGTH_SHORT).show()
                updateTransactions()
            }
        })

        // Fetch incomes
        apiService.getIncomes(userId).enqueue(object : Callback<List<IncomeResponse>> {
            override fun onResponse(call: Call<List<IncomeResponse>>, response: Response<List<IncomeResponse>>) {
                if (response.isSuccessful) filteredIncomes = filterByMonthYear(response.body() ?: emptyList()) { it.date }
                incomesLoaded = true
                updateTransactions()
            }

            override fun onFailure(call: Call<List<IncomeResponse>>, t: Throwable) {
                incomesLoaded = true
                Toast.makeText(this@ReportActivity, "Failed to load incomes: ${t.message}", Toast.LENGTH_SHORT).show()
                updateTransactions()
            }
        })
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            true // Scoped storage handles Android 11+
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            downloadReportFromBackend()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        }
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
        val userId = getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val month = spinnerMonth.selectedItemPosition + 1
        val year = spinnerYear.selectedItem.toString().toInt()

        apiService.getPdfReport(userId, month, year).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val filename = "ExpenseReport_${month}_${year}.pdf"
                    val inputStream = response.body()!!.byteStream()

                    try {
                        val outputStream: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // Save to Downloads using MediaStore on Android 10+
                            val contentValues = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, filename)
                                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }
                            val uri: Uri? = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            uri?.let { contentResolver.openOutputStream(it) }
                        } else {
                            // Save to external storage for older versions
                            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename)
                            FileOutputStream(file)
                        }

                        outputStream?.use { inputStream.copyTo(it) }
                        Toast.makeText(this@ReportActivity, "Report downloaded: $filename", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@ReportActivity, "Failed to save report: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReportActivity, "Failed to download report", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ReportActivity, "Failed to download report: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
