package com.example.expensetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

class dashboardActivity : AppCompatActivity() {

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
    private lateinit var lineChart: FrameLayout

    private lateinit var transactionAdapter: TransactionAdapter

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    private var userId: String = "" // Logged-in userId

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
//        lineChart = findViewById(R.id.chartContainer)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        menuIcon.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
            else drawerLayout.openDrawer(GravityCompat.START)
        }

        // Navigation menu listener
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


        // Get userId from SharedPreferences

        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""
        val username = prefs.getString("username", "")
        val email = prefs.getString("email", "")


        usernameText.text = "Hello, $username"
        val navHeader = navigationView.getHeaderView(0)
        navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, $username!"
        navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = email

        // Initialize selected month/year to current
        val cal = Calendar.getInstance()
        selectedMonth = cal.get(Calendar.MONTH)
        selectedYear = cal.get(Calendar.YEAR)
        updateMonthLabel()

        // Month picker
        btnSelectMonth.setOnClickListener { showMonthYearPicker() }

        // Load dashboard data for current month
        loadDashboardData(selectedMonth, selectedYear)
    }

    private fun showMonthYearPicker() {
        val cal = Calendar.getInstance()
        val dialog = DatePickerDialog(
            this,
            { _, year, month, _ ->
                selectedMonth = month
                selectedYear = year
                updateMonthLabel()
                loadDashboardData(selectedMonth, selectedYear)
            },
            selectedYear,
            selectedMonth,
            cal.get(Calendar.DAY_OF_MONTH)
        )

        // Hide day field
        val dayField = dialog.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )
        dayField?.visibility = View.GONE

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
        RetrofitClient.instance.getDashboardData(userId, month, year)
            .enqueue(object : retrofit2.Callback<DashboardResponse> {
                override fun onResponse(
                    call: retrofit2.Call<DashboardResponse>,
                    response: retrofit2.Response<DashboardResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val data = response.body()!!

                        // Update toolbar and nav header
                        usernameText.text = "Hello, ${data.username}"
                        val navHeader = findViewById<NavigationView>(R.id.navigation_view).getHeaderView(0)
                        navHeader.findViewById<TextView>(R.id.navHeaderName).text = "Hi, ${data.username}!"
                        navHeader.findViewById<TextView>(R.id.navHeaderEmail).text = data.email

                        // Totals
                        tvIncome.text = String.format("$%.2f", data.totalIncome)
                        tvExpense.text = String.format("$%.2f", data.totalExpense)
                        tvBalance.text = String.format("$%.2f", data.balance)

                        // Advice
                        tvAdvice.text = data.advice

                        // Recent transactions
                        transactionAdapter.updateData(data.recentTransactions)

                        // Line chart
                        //updateLineChart(data.dailyIncome, data.dailyExpense)
                        // After you get dashboard data
                        val lineChartView = findViewById<SimpleLineChartView>(R.id.lineChart)
                        lineChartView.setData(data.dailyIncome, data.dailyExpense)

                    }
                }

                override fun onFailure(call: retrofit2.Call<DashboardResponse>, t: Throwable) {
                    t.printStackTrace()
                }
            })
    }

//    private fun updateLineChart(dailyIncome: List<Double>, dailyExpense: List<Double>) {
//        val entriesIncome = dailyIncome.mapIndexed { index, value -> Entry(index.toFloat() + 1, value.toFloat()) }
//        val entriesExpense = dailyExpense.mapIndexed { index, value -> Entry(index.toFloat() + 1, value.toFloat()) }
//
//        val incomeDataSet = LineDataSet(entriesIncome, "Income").apply {
//            color = resources.getColor(R.color.green_500)
//            lineWidth = 2f
//            setDrawCircles(false)
//            setDrawValues(false)
//        }
//
//        val expenseDataSet = LineDataSet(entriesExpense, "Expense").apply {
//            color = resources.getColor(R.color.red_500)
//            lineWidth = 2f
//            setDrawCircles(false)
//            setDrawValues(false)
//        }
//
//        lineChart.apply {
//            data = LineData(incomeDataSet, expenseDataSet)
//            description.isEnabled = false
//            xAxis.position = XAxis.XAxisPosition.BOTTOM
//            axisRight.isEnabled = false
//            axisLeft.axisMinimum = 0f
//            invalidate()
//        }
//    }
}
