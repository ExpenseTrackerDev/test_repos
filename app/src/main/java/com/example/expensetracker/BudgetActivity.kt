package com.example.expensetracker

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BudgetActivity : AppCompatActivity() {

    private lateinit var budgetList: LinearLayout
    private lateinit var btnAddBudget: Button
    private lateinit var btnEditBudget: Button
    private lateinit var tvCurrentBudget: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        budgetList = findViewById(R.id.budgetList)
        btnAddBudget = findViewById(R.id.btnAddBudget)
        btnEditBudget = findViewById(R.id.btnEditBudget)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)

        btnAddBudget.setOnClickListener { showAddBudgetDialog() }

        // Sample budgets for testing
        addBudgetItem("January 2025", 70, 2000)
        addBudgetItem("February 2025", 45, 1800)
        addBudgetItem("March 2025", 90, 2500)
    }

    private fun addBudgetItem(month: String, usedPercent: Int, totalBudget: Int) {
        val view = layoutInflater.inflate(R.layout.budget_item, null)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonth)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBudget)
        val tvUsage = view.findViewById<TextView>(R.id.tvUsage)

        tvMonth.text = month
        progressBar.progress = usedPercent
        tvUsage.text = "Used: $usedPercent% of $$totalBudget"

        // Click to edit budget
        view.setOnClickListener {
            Toast.makeText(this, "Edit $month budget", Toast.LENGTH_SHORT).show()
        }

        budgetList.addView(view)
    }

    private fun showAddBudgetDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_budget, null)
        val etMonth = dialogView.findViewById<EditText>(R.id.etMonth)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveBudget)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val month = etMonth.text.toString()
            val amount = etAmount.text.toString().toIntOrNull() ?: 0

            if (month.isNotEmpty() && amount > 0) {
                addBudgetItem(month, 0, amount)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please enter valid month and amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

}
