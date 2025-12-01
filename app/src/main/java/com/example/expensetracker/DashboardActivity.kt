
package com.example.expensetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback


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

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0
    private var userId: String = ""

    private var logoutHandler: Handler? = null
    private val logoutRunnable = Runnable { logoutUser() }
    private val TAG = "DashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)


        bindViews()
        setupToolbarAndNavigation()
        setupBottomButtons()
        setupRecyclerView()
        loadSharedPreferencesData()
        setCurrentMonthYear()
        btnSelectMonth.setOnClickListener { showMonthYearPickerCustom() }
        loadDashboardData(selectedMonth, selectedYear)
    }

    private fun logoutUser() {
        // Clear all saved user data
        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Go to LoginActivity and clear back stack
        val intent = Intent(this, loginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    // Add these lifecycle overrides at class level
    override fun onPause() {
        super.onPause()
        // Start 5-minute timer when app goes to background
        logoutHandler = Handler(mainLooper)
        logoutHandler?.postDelayed(logoutRunnable, 5 * 60 * 1000) // 5 minutes
    }

    override fun onResume() {
        super.onResume()
        // Cancel auto-logout if user comes back before 5 minutes
        logoutHandler?.removeCallbacks(logoutRunnable)
    }


    private fun bindViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        tvSelectedMonth = findViewById(R.id.tvSelectedMonth)
        tvBalance = findViewById(R.id.tvBalance)
        tvIncome = findViewById(R.id.tvIncome)
        tvExpense = findViewById(R.id.tvExpense)
        tvAdvice = findViewById(R.id.tvAdvice)
        recyclerRecent = findViewById(R.id.recyclerRecent)
        btnSelectMonth = findViewById(R.id.btnSelectMonth)
        notificationIcon = findViewById(R.id.notification_icon)
        usernameText = findViewById(R.id.username_text)
        lineChartView = findViewById(R.id.lineChart)
    }

    private fun setupToolbarAndNavigation() {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        findViewById<ImageView>(R.id.menu_icon).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
            else drawerLayout.openDrawer(GravityCompat.START)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_income ->{
                    startActivity(Intent(this, IncomeActivity::class.java))
                    finish()}
                R.id.nav_expense -> {
                    startActivity(Intent(this, ExpenseActivity::class.java))
                    finish()
                }
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    finish()
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportActivity::class.java))
                    finish()
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, settingsActivity::class.java))
                    finish()
                }
                R.id.nav_logout -> logoutUser()
            }
            drawerLayout.closeDrawers()
            true
        }

        notificationIcon.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
            finish()
        }
    }

    private fun setupBottomButtons() {
        findViewById<Button>(R.id.btnBottomIncome).setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnBottomExpense).setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
            finish()
        }
        findViewById<Button>(R.id.btnBottomBudget).setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerRecent.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(emptyList())
        recyclerRecent.adapter = transactionAdapter
    }

    private fun loadSharedPreferencesData() {
        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""
        val username = prefs.getString("username", "") ?: ""
        val email = prefs.getString("email", "") ?: ""

        usernameText.text = "Hello, $username"
        val navHeader = findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)
        navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, $username!"
        navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = email
    }

    private fun setCurrentMonthYear() {
        val cal = Calendar.getInstance()
        selectedMonth = cal.get(Calendar.MONTH)
        selectedYear = cal.get(Calendar.YEAR)
        updateMonthLabel()
    }

    private fun updateMonthLabel() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, selectedMonth)
        cal.set(Calendar.YEAR, selectedYear)
        tvSelectedMonth.text = sdf.format(cal.time)
    }

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

        AlertDialog.Builder(this)
            .setTitle("Select month and year")
            .setView(layout)
            .setPositiveButton("OK") { _, _ ->
                selectedMonth = npMonth.value
                selectedYear = npYear.value
                updateMonthLabel()
                loadDashboardData(selectedMonth, selectedYear)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadDashboardData(month: Int, year: Int) {
        if (userId.isBlank()) {
            Toast.makeText(this, "Internal error: user not logged in", Toast.LENGTH_LONG).show()
            Log.e(TAG, "userId missing in SharedPreferences")
            return
        }

        RetrofitClient.instance.getDashboardData(userId, month + 1, year)
            .enqueue(object : Callback<DashboardResponse> {
                override fun onResponse(call: Call<DashboardResponse>, response: Response<DashboardResponse>) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@DashboardActivity, "Server returned ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val data = response.body()
                    if (data == null) {
                        Toast.makeText(this@DashboardActivity, "Failed to parse dashboard data", Toast.LENGTH_SHORT).show()
                        return
                    }

                    updateDashboardUI(data)
                    loadAllTransactionsForMonth(month, year)
                }

                override fun onFailure(call: Call<DashboardResponse>, t: Throwable) {
                    Toast.makeText(this@DashboardActivity, "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun updateDashboardUI(data: DashboardResponse) {
        usernameText.text = "Hello, ${data.username}"
        val navHeader = findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)
        navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, ${data.username}!"
        navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = data.email

        tvIncome.text = "Tk.${data.totalIncome ?: 0.0}"
        tvExpense.text = "Tk.${data.totalExpense ?: 0.0}"
        tvBalance.text = "Tk.${data.balance ?: 0.0}"
        val adviceText = when {
            data.hasPreviousData == false -> data.advice ?: "No previous month data available."
            !data.categoryAdvice.isNullOrBlank() -> data.categoryAdvice   // top priority
            !data.incomeUsageMessage.isNullOrBlank() -> data.incomeUsageMessage
            !data.advice.isNullOrBlank() -> data.advice
            else -> "Keep managing your finances wisely!"
        }

        tvAdvice.text = adviceText

        //tvAdvice.text = data.advice ?: ""

        lineChartView.setData(
            (data.dailyIncome ?: emptyList()).map { it.toFloat() },
            (data.dailyExpense ?: emptyList()).map { it.toFloat() }
        )
    }

    private fun loadAllTransactionsForMonth(month: Int, year: Int) {
        if (userId.isBlank()) return
        val datePrefix = String.format("%04d-%02d", year, month + 1)
        val allTransactions = mutableListOf<Transaction>()

        RetrofitClient.instance.getExpenses(userId)
            .enqueue(object : Callback<List<ExpenseResponse>> {
                override fun onResponse(call: Call<List<ExpenseResponse>>, response: Response<List<ExpenseResponse>>) {
                    val expenses = response.body()?.filter { it.date?.startsWith(datePrefix) == true } ?: emptyList()
                    allTransactions.addAll(expenses.map { Transaction("Expense", it.amount, it.category, it.description, it.date) })

                    RetrofitClient.instance.getIncomes(userId)
                        .enqueue(object : Callback<List<IncomeResponse>> {
                            override fun onResponse(call: Call<List<IncomeResponse>>, response: Response<List<IncomeResponse>>) {
                                val incomes = response.body()?.filter { it.date?.startsWith(datePrefix) == true } ?: emptyList()
                                allTransactions.addAll(incomes.map { Transaction("Income", it.amount, it.category, it.description, it.date) })
                                transactionAdapter.updateData(allTransactions.sortedByDescending { it.date ?: "" })
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
}
