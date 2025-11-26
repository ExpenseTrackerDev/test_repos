package com.example.expensetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.model.*
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var tvSelectedMonth: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvIncome: TextView
    private lateinit var tvExpense: TextView
    private lateinit var tvAdvice: TextView
    private lateinit var recyclerRecent: RecyclerView
    private lateinit var btnSelectMonth: ImageView
    private lateinit var notificationIcon: ImageView
    private lateinit var usernameText: TextView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var lineChartView: SimpleLineChartView

    private var selectedMonth: Int = 0 // 0..11
    private var selectedYear: Int = 0
    private var userId: String = ""

    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Bind views
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvAdvice = findViewById(R.id.tvAdvice)
        recyclerRecent = findViewById(R.id.recyclerRecent)
        notificationIcon = findViewById(R.id.notification_icon)
        usernameText = findViewById(R.id.username_text)
        lineChartView = findViewById(R.id.lineChart)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Menu toggle
        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
            else drawerLayout.openDrawer(GravityCompat.START)
        }

        // Navigation items
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_income -> startActivity(Intent(this, IncomeActivity::class.java))
                R.id.nav_expense -> startActivity(Intent(this, ExpenseActivity::class.java))
                R.id.nav_budget -> startActivity(Intent(this, BudgetActivity::class.java))
                R.id.nav_reports -> startActivity(Intent(this, ReportActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, settingsActivity::class.java))
                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawers()
            true
        }

        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        // Bottom buttons
        findViewById<Button>(R.id.btnBottomIncome).setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
        }
        findViewById<Button>(R.id.btnBottomExpense).setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
        }
        findViewById<Button>(R.id.btnBottomBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // RecyclerView setup
        recyclerRecent.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        recyclerRecent.adapter = transactionAdapter

        // SharedPreferences
        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""
        val username = prefs.getString("username", "") ?: ""
        val email = prefs.getString("email", "") ?: ""

        usernameText.text = "Hello, $username"
        val navHeader = navigationView.getHeaderView(0)
        navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, $username!"
        navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = email

        // Current month/year
        val cal = Calendar.getInstance()
        selectedMonth = cal.get(Calendar.MONTH)
        selectedYear = cal.get(Calendar.YEAR)
        updateMonthLabel()

        // Month picker (custom)
        btnSelectMonth.setOnClickListener { showMonthYearPickerCustom() }

        // Load dashboard data
        loadDashboardData(selectedMonth, selectedYear)
    }

    /**
     * Custom month-year picker using two NumberPickers inside an AlertDialog.
     * This is reliable across Android versions.
     */
    private fun showMonthYearPickerCustom() {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )

        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val minYear = currentYear - 10
        val maxYear = currentYear + 5

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 24, 24, 12)
            gravity = Gravity.CENTER
        }

        val npMonth = NumberPicker(this).apply {
            minValue = 0
            maxValue = 11
            displayedValues = months
            value = selectedMonth
            wrapSelectorWheel = false
        }

        val npYear = NumberPicker(this).apply {
            minValue = minYear
            maxValue = maxYear
            value = selectedYear
            wrapSelectorWheel = false
        }

        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        layout.addView(npMonth, lp)
        layout.addView(npYear, lp)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select month and year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                selectedMonth = npMonth.value
                selectedYear = npYear.value
                updateMonthLabel()
                loadDashboardData(selectedMonth, selectedYear)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateMonthLabel() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth)
        cal.set(Calendar.YEAR, selectedYear)
        tvSelectedMonth.text = sdf.format(cal.time)
    }

    private fun loadDashboardData(month: Int, year: Int) {
        if (userId.isBlank()) {
            Log.e(TAG, "userId is empty. Make sure user is logged in and userId stored in SharedPreferences.")
            Toast.makeText(this, "Internal: user not logged in (userId missing)", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "Requesting dashboard for userId=$userId month=${month+1} year=$year")

        RetrofitClient.instance.getDashboardData(userId, month + 1, year)
            .enqueue(object : Callback<DashboardResponse> {
                override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                    Log.d(TAG, "Retrofit onResponse: code=${response.code()}")
                    if (!response.isSuccessful) {
                        Log.e(TAG, "API returned error code ${response.code()} - ${response.message()}")
                        Toast.makeText(this@DashboardActivity, "Server returned ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = response.body()
                    Log.d(TAG, "Raw body -> $data")
                    if (data == null) {
                        Log.e(TAG, "Response body parsed to null. Check DashboardResponse model & field names.")
                        Toast.makeText(this@DashboardActivity, "Parsing error: response body null", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // Update user info
                    usernameText.text = "Hello, ${data.username}"
                    val navHeader = findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)
                    navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, ${data.username}!"
                    navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = data.email

                    // Update summary cards safely
                    tvIncome.text = "tk.${data.totalIncome}"
                    tvExpense.text = "Tk.${data.totalExpense}"
                    tvBalance.text = "Tk.${data.balance}"
                    tvAdvice.text = data.advice ?: ""

                    // NEW: load all transactions of current month
                    loadAllTransactionsForMonth(month, year)

                    // Update chart (safe)
                    val incomes = data.dailyIncome ?: emptyList()
                    val expenses = data.dailyExpense ?: emptyList()
                    lineChartView.setData(incomes.map { it.toFloat() }, expenses.map { it.toFloat() })

                    Toast.makeText(this@DashboardActivity, "Dashboard loaded", Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    Log.e(TAG, "API call failed", t)
                    Toast.makeText(this@DashboardActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    /*** NEW METHODS TO FETCH ALL TRANSACTIONS ***/
    /*** NEW METHODS TO FETCH ALL TRANSACTIONS ***/
    private fun loadAllTransactionsForMonth(month: Int, year: Int) {
        if (userId.isBlank()) return

        val monthStr = String.format("%02d", month + 1)
        val datePrefix = "$year-$monthStr" // e.g., "2025-11"

        val allTransactions = mutableListOf<Transaction>()

        // Step 1: Fetch all expenses
        RetrofitClient.instance.getExpenses(userId)
            .enqueue(object : Callback<List<ExpenseResponse>> {
                override fun onResponse(call: Call<List<ExpenseResponse>>, response: Response<List<ExpenseResponse>>) {
                    val expenses = response.body()?.filter { it.date.startsWith(datePrefix) } ?: emptyList()
                    allTransactions.addAll(expenses.map {
                        Transaction("Expense", it.amount, it.category, it.description, it.date)
                    })

                    // Step 2: Fetch all incomes
                    RetrofitClient.instance.getIncomes(userId)
                        .enqueue(object : Callback<List<IncomeResponse>> {
                            override fun onResponse(call: Call<List<IncomeResponse>>, response: Response<List<IncomeResponse>>) {
                                val incomes = response.body()?.filter { it.date.startsWith(datePrefix) } ?: emptyList()
                                allTransactions.addAll(incomes.map {
                                    Transaction("Income", it.amount, it.category, it.description, it.date)
                                })

                                // Step 3: Sort all transactions by date descending
                                val sortedTransactions = allTransactions.sortedByDescending { it.date ?: "" }

                                // Step 4: Update RecyclerView
                                transactionAdapter.updateData(sortedTransactions)
                            }

                            override fun onFailure(call: Call<List<IncomeResponse>>, t: Throwable) {
                                Toast.makeText(this@DashboardActivity, "Failed to load incomes", Toast.LENGTH_SHORT).show()
                            }
                        })
                }

                override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun loadIncomesForMonth(datePrefix: String, allTransactions: MutableList<Transaction>) {
        RetrofitClient.instance.filterIncomes(userId, null, datePrefix)
            .enqueue(object : Callback<List<IncomeResponse>> {
                override fun onResponse(call: Call<List<IncomeResponse>>, response: Response<List<IncomeResponse>>) {
                    val incomes = response.body() ?: emptyList()
                    allTransactions.addAll(incomes.map {
                        Transaction("Income", it.amount, it.category, it.description, it.date)
                    })

                    // Sort by date descending
                    val sorted = allTransactions.sortedByDescending { it.date ?: "" }
                    transactionAdapter.updateData(sorted)
                }

                override fun onFailure(call: Call<List<IncomeResponse>>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Failed to load incomes", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
