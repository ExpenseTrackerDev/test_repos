package com.example.expensetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.*

class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetList: LinearLayout
    private lateinit var btnAddBudget: Button
    private lateinit var btnBack: ImageView
    private lateinit var tvCurrentBudget: TextView
    private lateinit var spinnerYear: Spinner

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    private val userId by lazy {
        getSharedPreferences("userPrefs", MODE_PRIVATE).getString("userId", "") ?: ""
    }

    private var allBudgets = mutableListOf<BudgetResponse>()
    private var availableYears = mutableSetOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        budgetList = findViewById(R.id.budgetList)
        btnAddBudget = findViewById(R.id.btnAddBudget)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)
        btnBack = findViewById(R.id.back_btn)
        spinnerYear = findViewById(R.id.spinnerYear)

        btnBack.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        btnAddBudget.setOnClickListener { showAddBudgetDialog() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchAllBudgets()
    }

    /** ------------------------------------------------------------
     *  Fetch ALL budgets (not just one year) — main fix
     *  ------------------------------------------------------------- */
    private fun fetchAllBudgets() {
        RetrofitClient.instance.getAllBudgets(userId)
            .enqueue(object : retrofit2.Callback<List<BudgetResponse>> {
                override fun onResponse(
                    call: retrofit2.Call<List<BudgetResponse>>,
                    response: retrofit2.Response<List<BudgetResponse>>
                ) {
                    if (!response.isSuccessful || response.body() == null) {
                        Toast.makeText(this@BudgetActivity, "Failed to load budgets", Toast.LENGTH_SHORT).show()
                        return
                    }

                    allBudgets = response.body()!!.toMutableList()

                    /** Build year list */
                    availableYears.clear()
                    allBudgets.forEach { availableYears.add(it.year) }
                    availableYears.add(currentYear)
                    availableYears.add(currentYear + 1)

                    val yearsList = availableYears.sorted()

                    spinnerYear.adapter = ArrayAdapter(
                        this@BudgetActivity,
                        android.R.layout.simple_spinner_item,
                        yearsList
                    )

                    /** Spinner listener */
                    spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedYear = yearsList[position]
                            updateUIForYear(selectedYear)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

                    /** Show current year by default */
                    val currentIndex = yearsList.indexOf(currentYear)
                    spinnerYear.setSelection(if (currentIndex != -1) currentIndex else 0)

                    updateUIForYear(currentYear)
                }

                override fun onFailure(call: retrofit2.Call<List<BudgetResponse>>, t: Throwable) {
                    Toast.makeText(this@BudgetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /** ------------------------------------------------------------
     *  UI Update
     *  ------------------------------------------------------------- */
    private fun updateUIForYear(year: Int) {
        val budgetsThisYear = allBudgets.filter { it.year == year }.sortedBy { it.month }

        /** Current month’s budget card */
        val currentBudget = allBudgets.find { it.year == currentYear && it.month == currentMonth }
        tvCurrentBudget.text = "$${currentBudget?.amount ?: 0}"

        /** Create list */
        budgetList.removeAllViews()
        if (budgetsThisYear.isEmpty()) {
            val tvEmpty = TextView(this)
            tvEmpty.text = "No budgets set for this year"
            tvEmpty.textSize = 14f
            budgetList.addView(tvEmpty)
        } else {
            budgetsThisYear.forEach { addBudgetItem(it) }
        }
    }

    private fun addBudgetItem(budget: BudgetResponse) {
        val view = layoutInflater.inflate(R.layout.budget_item, null)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonth)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvUsage = view.findViewById<TextView>(R.id.tvUsage)
        val btnEdit = view.findViewById<Button>(R.id.btnEditBudgetItem)

        val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
            .format(Calendar.getInstance().apply { set(Calendar.MONTH, budget.month - 1) }.time)

        tvMonth.text = "$monthName ${budget.year}"
        progressBar.progress = budget.percentUsed
        tvUsage.text = "Used: ${budget.percentUsed}% of $${budget.amount}" +
                if (budget.overBudget > 0) " (Over by $${budget.overBudget})" else ""

        btnEdit.visibility = if (budget.editable) View.VISIBLE else View.GONE
        btnEdit.setOnClickListener { showEditBudgetDialog(budget) }

        budgetList.addView(view)
    }

    /** ------------------------------------------------------------
     *  Add Budget Dialog
     *  ------------------------------------------------------------- */
    private fun showAddBudgetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
        val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerYearDialog = dialogView.findViewById<Spinner>(R.id.spinnerYearDialog)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)

        val years = (availableYears + listOf(currentYear, currentYear + 1)).toSet().sorted()
        spinnerYearDialog.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)

        fun updateMonths(selectedYear: Int) {
            val usedMonths = allBudgets.filter { it.year == selectedYear }.map { it.month }
            val startMonth = if (selectedYear == currentYear) currentMonth else 1
            val availMonths = (startMonth..12).filter { it !in usedMonths }

            val names = availMonths.map {
                SimpleDateFormat("MMMM", Locale.getDefault())
                    .format(Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time)
            }

            spinnerMonth.tag = availMonths.toIntArray()
            spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        }

        updateMonths(currentYear)

        spinnerYearDialog.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, id: Long) {
                updateMonths(years[pos])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnSave.setOnClickListener {
            val selectedYear = spinnerYearDialog.selectedItem as Int
            val availableMonths = spinnerMonth.tag as IntArray
            val month = availableMonths[spinnerMonth.selectedItemPosition]
            val amount = etAmount.text.toString().toIntOrNull() ?: 0

            if (amount <= 0) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addBudgetApi(month, selectedYear, amount)
            dialog.dismiss()
        }

        dialog.show()
    }

    /** ------------------------------------------------------------
     *  Edit Budget Dialog (Fixed)
     *  ------------------------------------------------------------- */
//    private fun showEditBudgetDialog(budget: BudgetResponse) {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)
//
//        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
//        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)
//        val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
//        val spinnerYear = dialogView.findViewById<Spinner>(R.id.spinnerYearDialog)
//        val tvMonthEdit = dialogView.findViewById<TextView>(R.id.tvMonthEdit)
//        val tvYearEdit = dialogView.findViewById<TextView>(R.id.tvYearEdit)
//        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
//
//        spinnerMonth.visibility = View.GONE
//        spinnerYear.visibility = View.GONE
//        tvMonthEdit.visibility = View.VISIBLE
//        tvYearEdit.visibility = View.VISIBLE
//
//        tvDialogTitle.text = "Edit Budget"
//
//        val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
//            .format(Calendar.getInstance().apply { set(Calendar.MONTH, budget.month - 1) }.time)
//
//        tvMonthEdit.text = monthName
//        tvYearEdit.text = budget.year.toString()
//
//        etAmount.setText(budget.amount.toString())
//
//        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
//
//        btnSave.setOnClickListener {
//            val amount = etAmount.text.toString().toIntOrNull() ?: 0
//            if (amount <= 0) {
//                Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            editBudgetApi(budget.budgetId, amount)
//            dialog.dismiss()
//        }
//
//        dialog.show()
//    }

    private fun showEditBudgetDialog(budget: BudgetResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_budget, null)

        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)
        val spinnerMonth = dialogView.findViewById<Spinner>(R.id.spinnerMonth)
        val spinnerYear = dialogView.findViewById<Spinner>(R.id.spinnerYearDialog)
        val tvMonthEdit = dialogView.findViewById<TextView>(R.id.tvMonthEdit)
        val tvYearEdit = dialogView.findViewById<TextView>(R.id.tvYearEdit)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        spinnerMonth.visibility = View.GONE
        spinnerYear.visibility = View.GONE
        tvMonthEdit.visibility = View.VISIBLE
        tvYearEdit.visibility = View.VISIBLE

        tvDialogTitle.text = "Edit Budget"

        val monthName = SimpleDateFormat("MMMM", Locale.getDefault())
            .format(Calendar.getInstance().apply { set(Calendar.MONTH, budget.month - 1) }.time)
        tvMonthEdit.text = monthName
        tvYearEdit.text = budget.year.toString()

        etAmount.setText(budget.amount.toString())

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        btnSave.setOnClickListener {
            val newAmount = etAmount.text.toString().toIntOrNull() ?: 0
            if (newAmount <= 0) {
                Toast.makeText(this, "Enter valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // API call
            RetrofitClient.instance.editBudget(budget.budgetId, EditBudgetRequest(newAmount), userId)
                .enqueue(object : retrofit2.Callback<BudgetResponse> {
                    override fun onResponse(
                        call: retrofit2.Call<BudgetResponse>,
                        response: retrofit2.Response<BudgetResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@BudgetActivity, "Budget updated", Toast.LENGTH_SHORT).show()
                            fetchAllBudgets()  // Refresh UI
                        } else {
                            Toast.makeText(
                                this@BudgetActivity,
                                "Failed to update: ${response.message()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<BudgetResponse>, t: Throwable) {
                        Toast.makeText(this@BudgetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            editBudgetApi(budget.budgetId, newAmount)
            dialog.dismiss()
        }

        dialog.show()
    }

    /** ------------------------------------------------------------
     *  API Calls
     *  ------------------------------------------------------------- */
    private fun addBudgetApi(month: Int, year: Int, amount: Int) {
        RetrofitClient.instance.addBudget(AddBudgetRequest(month, year, amount), userId)
            .enqueue(object : retrofit2.Callback<BudgetResponse> {
                override fun onResponse(call: retrofit2.Call<BudgetResponse>, response: retrofit2.Response<BudgetResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BudgetActivity, "Budget added", Toast.LENGTH_SHORT).show()
                        fetchAllBudgets()
                    } else {
                        Toast.makeText(this@BudgetActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<BudgetResponse>, t: Throwable) {
                    Toast.makeText(this@BudgetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun editBudgetApi(budgetId: String, amount: Int) {
        Log.d("BudgetActivity", "API call: budgetId=$budgetId, amount=$amount, userId=$userId")
        RetrofitClient.instance.editBudget(budgetId, EditBudgetRequest(amount), userId)
            .enqueue(object : retrofit2.Callback<BudgetResponse> {
                override fun onResponse(
                    call: retrofit2.Call<BudgetResponse>,
                    response: retrofit2.Response<BudgetResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@BudgetActivity, "Budget updated", Toast.LENGTH_SHORT).show()
                        fetchAllBudgets()
                    } else {
                        Log.e("BudgetActivity", "Failed: ${response.code()} ${response.message()}")
                        Toast.makeText(this@BudgetActivity, "Failed to update", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<BudgetResponse>, t: Throwable) {
                    Toast.makeText(this@BudgetActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
