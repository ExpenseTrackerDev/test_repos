package com.example.expensetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerExpense: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)


//        setSupportActionBar(findViewById(R.id.topBar))
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerExpense = findViewById(R.id.recyclerExpense)
        recyclerExpense.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(expenseList, ::editExpense, ::deleteExpense)
        recyclerExpense.adapter = expenseAdapter

        val btnBack = findViewById<ImageView>(R.id.back_btn)

        btnBack.setOnClickListener {
            val intent = Intent(this, dashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<FloatingActionButton>(R.id.btnAddExpense).setOnClickListener {
            showAddExpenseDialog()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showAddExpenseDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = etCategory.text.toString()
            val date = etDate.text.toString()
            val description = etDescription.text.toString()

            expenseList.add(Expense(category, amount, date, description))
            expenseAdapter.notifyItemInserted(expenseList.size - 1)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun editExpense(position: Int) {
        val expense = expenseList[position]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)

        etAmount.setText(expense.amount.toString())
        etCategory.setText(expense.category)
        etDate.setText(expense.date)
        etDescription.setText(expense.description)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            expense.amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            expense.category = etCategory.text.toString()
            expense.date = etDate.text.toString()
            expense.description = etDescription.text.toString()
            expenseAdapter.notifyItemChanged(position)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteExpense(position: Int) {
        expenseList.removeAt(position)
        expenseAdapter.notifyItemRemoved(position)
    }
}
