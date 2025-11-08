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

class IncomeActivity : AppCompatActivity() {

    private lateinit var recyclerIncome: RecyclerView
    private lateinit var incomeAdapter: IncomeAdapter
    private val incomeList = mutableListOf<Income>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)


//        setSupportActionBar(findViewById(R.id.topBar))
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val btnBack = findViewById<ImageView>(R.id.back_btn)

        btnBack.setOnClickListener {
            val intent = Intent(this, dashboardActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerIncome = findViewById(R.id.recyclerIncome)
        recyclerIncome.layoutManager = LinearLayoutManager(this)
        incomeAdapter = IncomeAdapter(incomeList, ::editIncome, ::deleteIncome)
        recyclerIncome.adapter = incomeAdapter


        findViewById<FloatingActionButton>(R.id.btnAddIncome).setOnClickListener {
            showAddIncomeDialog()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun showAddIncomeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etIncomeCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etIncomeDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etIncomeDescription)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = etCategory.text.toString()
            val date = etDate.text.toString()
            val description = etDescription.text.toString()

            incomeList.add(Income(category, amount, date, description))
            incomeAdapter.notifyItemInserted(incomeList.size - 1)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun editIncome(position: Int) {
        val income = incomeList[position]
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etIncomeCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etIncomeDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etIncomeDescription)

        etAmount.setText(income.amount.toString())
        etCategory.setText(income.category)
        etDate.setText(income.date)
        etDescription.setText(income.description)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            income.amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            income.category = etCategory.text.toString()
            income.date = etDate.text.toString()
            income.description = etDescription.text.toString()
            incomeAdapter.notifyItemChanged(position)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteIncome(position: Int) {
        incomeList.removeAt(position)
        incomeAdapter.notifyItemRemoved(position)
    }
}
