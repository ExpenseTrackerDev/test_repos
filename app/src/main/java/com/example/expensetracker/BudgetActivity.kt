package com.example.expensetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*



class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetList: LinearLayout
    private lateinit var btnAddBudget: Button
    private lateinit var btnEditBudget: Button
    private lateinit var tvCurrentBudget: TextView
    private lateinit var btnBack: ImageView
    private lateinit var spinnerYear: Spinner

    private val budgets = mutableListOf<Budget>()
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        budgetList = findViewById(R.id.budgetList)
        btnAddBudget = findViewById(R.id.btnAddBudget)
        btnEditBudget = findViewById(R.id.btnEditBudget)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)
        btnBack = findViewById(R.id.back_btn)
        spinnerYear = findViewById(R.id.spinnerYear)

        btnBack.setOnClickListener {
            startActivity(Intent(this, dashboardActivity::class.java))
            finish()
        }

        // Populate year spinner
        val years = (currentYear - 5..currentYear + 1).toList().reversed()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
        spinnerYear.setSelection(years.indexOf(currentYear))
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                displayBudgets(years[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnAddBudget.setOnClickListener { showAddBudgetDialog() }

        btnEditBudget.setOnClickListener {
            editCurrentBudget()
        }

        // Sample data
        budgets.add(Budget(currentYear, 1, 2000, 70))
        budgets.add(Budget(currentYear, 2, 1800, 45))
        budgets.add(Budget(currentYear, 3, 2500, 90))

        displayBudgets(currentYear)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun displayBudgets(year: Int) {
        budgetList.removeAllViews()
        val monthBudgets = budgets.filter { it.year == year }.sortedBy { it.month }

        // Current month budget
        val current = monthBudgets.find { it.month == currentMonth }
        if (current != null) {
            tvCurrentBudget.text = "$${current.amount}"
        } else {
            tvCurrentBudget.text = "$0"
        }

        // Display all month budgets
        if (monthBudgets.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No budgets set for this year"
            tvEmpty.textSize = 14f
            budgetList.addView(tvEmpty)
        } else {
            for (b in monthBudgets) {
                addBudgetItem(b)
            }
        }
    }

    private fun addBudgetItem(budget: Budget) {
        val view = layoutInflater.inflate(R.layout.budget_item, null)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonth)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvUsage = view.findViewById<TextView>(R.id.tvUsage)

        val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(
            Calendar.getInstance().apply { set(Calendar.MONTH, budget.month - 1) }.time
        )

        tvMonth.text = "$monthName ${budget.year}"
        progressBar.progress = budget.usedPercent
        tvUsage.text = "Used: ${budget.usedPercent}% of $${budget.amount}"

        view.setOnClickListener {
            Toast.makeText(this, "Edit $monthName ${budget.year} budget", Toast.LENGTH_SHORT).show()
        }

        budgetList.addView(view)
    }

    private fun showAddBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
        val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerYearDialog = dialogView.findViewById<Spinner>(R.id.spinnerYearDialog)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)

        val months = (1..12).map { SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time) }
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter
        spinnerMonth.setSelection(currentMonth - 1)

        val years = (currentYear - 5..currentYear + 1).toList()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYearDialog.adapter = yearAdapter
        spinnerYearDialog.setSelection(years.indexOf(currentYear))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val month = spinnerMonth.selectedItemPosition + 1
            val year = spinnerYearDialog.selectedItem as Int
            val amount = etAmount.text.toString().toIntOrNull() ?: 0

            if (amount > 0) {
                // Check if budget exists for same month/year
                val existing = budgets.find { it.year == year && it.month == month }
                if (existing != null) {
                    existing.amount = amount
                } else {
                    budgets.add(Budget(year, month, amount, 0))
                }
                displayBudgets(spinnerYear.selectedItem as Int)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun editCurrentBudget() {
        val currentBudget = budgets.find { it.year == currentYear && it.month == currentMonth }
        if (currentBudget != null) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
            val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
            val spinnerYearDialog = dialogView.findViewById<Spinner>(R.id.spinnerYearDialog)
            val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)

            val months = (1..12).map { SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time) }
            val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
            monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMonth.adapter = monthAdapter
            spinnerMonth.setSelection(currentMonth - 1)
            spinnerMonth.isEnabled = false

            val years = (currentYear - 5..currentYear + 1).toList()
            val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerYearDialog.adapter = yearAdapter
            spinnerYearDialog.setSelection(years.indexOf(currentYear))
            spinnerYearDialog.isEnabled = false

            etAmount.setText(currentBudget.amount.toString())

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnSave.setOnClickListener {
                val amount = etAmount.text.toString().toIntOrNull() ?: 0
                if (amount > 0) {
                    currentBudget.amount = amount
                    displayBudgets(currentYear)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        } else {
            Toast.makeText(this, "No budget set for current month", Toast.LENGTH_SHORT).show()
        }
    }
}

