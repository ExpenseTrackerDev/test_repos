package com.example.expensetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class ExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerExpense: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private val filteredList = mutableListOf<Expense>()

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var etSearchCategory: EditText
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        val btnBack = findViewById<ImageView>(R.id.back_btn)
        btnBack.setOnClickListener {
            startActivity(Intent(this, dashboardActivity::class.java))
            finish()
        }

        recyclerExpense = findViewById(R.id.recyclerExpense)
        recyclerExpense.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(filteredList, ::editExpense, ::deleteExpense)
        recyclerExpense.adapter = expenseAdapter

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)
        etSearchCategory = findViewById(R.id.etSearchCategory)
        etFromDate = findViewById(R.id.etFromDate)
//        etToDate = findViewById(R.id.etToDate)
        val btnClearFilter = findViewById<Button>(R.id.btnClearFilter)
        btnClearFilter.setOnClickListener {
            etFromDate.text.clear()
            etSearchCategory.text.clear()

            // Reload the full list for the current month
            filteredList.clear()

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()

            for (expense in expenseList) {
                val date = try { format.parse(expense.date.trim()) } catch (e: Exception) { null } ?: continue
                val cal = Calendar.getInstance()
                cal.time = date

                // Only include incomes from current month and year
                if (cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    filteredList.add(expense)
                }
            }

            expenseAdapter.notifyDataSetChanged()           // Reload incomes for current month
        }


        findViewById<FloatingActionButton>(R.id.btnAddExpense).setOnClickListener {
            showAddExpenseDialog()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize spinners and filters
        setupMonthYearSpinners()
        setupCategorySearch()
        setupDatePickers()

        // Load sample data
        loadSampleExpenses()
    }

    private fun loadSampleExpenses() {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        expenseList.add(Expense("Food", 500.0, format.format(calendar.time), "Lunch at office"))
        expenseList.add(Expense("Transport", 120.0, format.format(calendar.time), "Taxi fare"))

        // Initially show current month expenses
        filterExpenses()
    }

    private fun setupMonthYearSpinners() {
        val months = (1..12).map { it.toString() }
        val years = (2023..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() }

        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)

        val calendar = Calendar.getInstance()
        spinnerMonth.setSelection(calendar.get(Calendar.MONTH))
        spinnerYear.setSelection(years.indexOf(calendar.get(Calendar.YEAR).toString()))

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterExpenses()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerMonth.onItemSelectedListener = listener
        spinnerYear.onItemSelectedListener = listener
    }

    private fun setupCategorySearch() {
        etSearchCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterExpenses() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDatePickers() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val listener = { editText: EditText ->
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    editText.setText(format.format(c.time))
                    filterExpenses()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        etFromDate.setOnClickListener { listener(etFromDate) }
//        etToDate.setOnClickListener { listener(etToDate) }
    }

//    private fun filterExpenses() {
//        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
//        val selectedYear = spinnerYear.selectedItem.toString().toInt()
//        val categoryQuery = etSearchCategory.text.toString().lowercase()
//        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        filteredList.clear()
//        for (expense in expenseList) {
//            val date = format.parse(expense.date)
//            val cal = Calendar.getInstance()
//            cal.time = date!!
//
//            if (cal.get(Calendar.MONTH) + 1 == selectedMonth &&
//                cal.get(Calendar.YEAR) == selectedYear &&
//                expense.category.lowercase().contains(categoryQuery)) {
//
//                // Filter by date range if specified
//                val fromDate = if (etFromDate.text.isNotEmpty()) format.parse(etFromDate.text.toString()) else null
//                val toDate = if (etToDate.text.isNotEmpty()) format.parse(etToDate.text.toString()) else null
//
//                if ((fromDate == null || !date.before(fromDate)) &&
//                    (toDate == null || !date.after(toDate))) {
//                    filteredList.add(expense)
//                }
//            }
//        }
//        expenseAdapter.notifyDataSetChanged()
//    }

    private fun filterExpenses() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDate = if (etFromDate.text.isNotEmpty()) {
            try {
                format.parse(etFromDate.text.toString().trim())
            } catch (e: Exception) {
                null
            }
        } else null

        val categoryQuery = etSearchCategory.text.toString().trim().lowercase()

        filteredList.clear()

        for (expense in expenseList) {
            val incomeDate = try {
                format.parse(expense.date.trim())
            } catch (e: Exception) {
                null
            } ?: continue
            val incomeCategory = expense.category.trim().lowercase()

            // Check if it matches category filter
            val matchesCategory = categoryQuery.isEmpty() || incomeCategory.contains(categoryQuery)

            // Check if it matches exact From Date
            val matchesDate = fromDate == null || incomeDate == fromDate

            if (matchesCategory && matchesDate) {
                filteredList.add(expense)
            }
        }

        expenseAdapter.notifyDataSetChanged()
    }


    private fun showAddExpenseDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    etDate.setText(format.format(c.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = etCategory.text.toString()
            val date = etDate.text.toString()
            val description = etDescription.text.toString()

            expenseList.add(Expense(category, amount, date, description))
            filterExpenses()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun editExpense(position: Int) {
        val expense = filteredList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)

        etAmount.setText(expense.amount.toString())
        etCategory.setText(expense.category)
        etDate.setText(expense.date)
        etDescription.setText(expense.description)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    etDate.setText(format.format(c.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            expense.amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            expense.category = etCategory.text.toString()
            expense.date = etDate.text.toString()
            expense.description = etDescription.text.toString()
            filterExpenses()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteExpense(position: Int) {
        val expense = filteredList[position]

        // Create confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("OK") { dialog, _ ->
                // Delete expense if OK is pressed
                expenseList.remove(expense)
                filterExpenses()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
