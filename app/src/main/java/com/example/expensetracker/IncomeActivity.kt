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

class IncomeActivity : AppCompatActivity() {

    private lateinit var recyclerIncome: RecyclerView
    private lateinit var incomeAdapter: IncomeAdapter
    private val incomeList = mutableListOf<Income>()
    private var filteredList = mutableListOf<Income>()

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var etSearchCategory: EditText
    private lateinit var etFromDate: EditText
    private lateinit var etToDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        val btnBack = findViewById<ImageView>(R.id.back_btn)
        btnBack.setOnClickListener {
            startActivity(Intent(this, dashboardActivity::class.java))
            finish()
        }

        recyclerIncome = findViewById(R.id.recyclerIncome)
        recyclerIncome.layoutManager = LinearLayoutManager(this)
        incomeAdapter = IncomeAdapter(filteredList, ::editIncome, ::deleteIncome)
        recyclerIncome.adapter = incomeAdapter

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)
        etSearchCategory = findViewById(R.id.etSearchCategory)
        etFromDate = findViewById(R.id.etFromDate)
//        etToDate = findViewById(R.id.etToDate)
        val btnClearFilter = findViewById<Button>(R.id.btnClearFilter)

//        btnClearFilter.setOnClickListener {
//            // Reset all filter inputs
//            spinnerMonth.setSelection(0)   // assumes first item is "All" or blank
//            spinnerYear.setSelection(0)
//            etSearchCategory.text.clear()
//            etFromDate.text.clear()
//            // etToDate.text.clear() // if you are using a To Date field
//            filterIncomes()
//
//            // Reload the full income list
////            incomeAdapter.updateList(fullIncomeList) // fullIncomeList = original unfiltered data
//        }
        btnClearFilter.setOnClickListener {
            // Clear all filter inputs
            etFromDate.text.clear()
            etSearchCategory.text.clear()

            // Reload the full list for the current month
            filteredList.clear()

            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val today = Calendar.getInstance()

            for (income in incomeList) {
                val date = try { format.parse(income.date.trim()) } catch (e: Exception) { null } ?: continue
                val cal = Calendar.getInstance()
                cal.time = date

                // Only include incomes from current month and year
                if (cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                    filteredList.add(income)
                }
            }

            incomeAdapter.notifyDataSetChanged()
        }




        findViewById<FloatingActionButton>(R.id.btnAddIncome).setOnClickListener {
            showAddIncomeDialog()
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
        loadSampleIncomes()
    }

    private fun loadSampleIncomes() {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        incomeList.add(Income("Salary", 25000.0, format.format(calendar.time), "October Salary"))
        incomeList.add(Income("Freelance", 8000.0, format.format(calendar.time), "Project Payment"))

        // Initially show current month incomes
        filterIncomes()
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
                filterIncomes()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        spinnerMonth.onItemSelectedListener = listener
        spinnerYear.onItemSelectedListener = listener
    }

    private fun setupCategorySearch() {
        etSearchCategory.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterIncomes() }
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
                    filterIncomes()
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

//    private fun filterIncomes() {
//        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
//        val selectedYear = spinnerYear.selectedItem.toString().toInt()
//        val categoryQuery = etSearchCategory.text.toString().lowercase()
//        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//
//        filteredList.clear()
//        for (income in incomeList) {
//            val date = format.parse(income.date)
//            val cal = Calendar.getInstance()
//            cal.time = date!!
//
//            if (cal.get(Calendar.MONTH) + 1 == selectedMonth &&
//                cal.get(Calendar.YEAR) == selectedYear &&
//                income.category.lowercase().contains(categoryQuery)) {
//
//                // Filter by date range if specified
//                val fromDate = if (etFromDate.text.isNotEmpty()) format.parse(etFromDate.text.toString()) else null
////                val toDate = if (etToDate.text.isNotEmpty()) format.parse(etToDate.text.toString()) else null
//
//                if ((fromDate == null || !date.before(fromDate))) {
//                    filteredList.add(income)
//                }
//            }
//        }
//        incomeAdapter.notifyDataSetChanged()
//    }


    private fun filterIncomes() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDate = if (etFromDate.text.isNotEmpty()) {
            try { format.parse(etFromDate.text.toString().trim()) } catch (e: Exception) { null }
        } else null

        val categoryQuery = etSearchCategory.text.toString().trim().lowercase()

        filteredList.clear()

        for (income in incomeList) {
            val incomeDate = try { format.parse(income.date.trim()) } catch (e: Exception) { null } ?: continue
            val incomeCategory = income.category.trim().lowercase()

            // Check if it matches category filter
            val matchesCategory = categoryQuery.isEmpty() || incomeCategory.contains(categoryQuery)

            // Check if it matches exact From Date
            val matchesDate = fromDate == null || incomeDate == fromDate

            if (matchesCategory && matchesDate) {
                filteredList.add(income)
            }
        }

        incomeAdapter.notifyDataSetChanged()
    }






    private fun showAddIncomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etIncomeCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etIncomeDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etIncomeDescription)

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

        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            val category = etCategory.text.toString()
            val date = etDate.text.toString()
            val description = etDescription.text.toString()

            incomeList.add(Income(category, amount, date, description))
            filterIncomes()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun editIncome(position: Int) {
        val income = filteredList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_income, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etIncomeAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etIncomeCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etIncomeDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etIncomeDescription)

        etAmount.setText(income.amount.toString())
        etCategory.setText(income.category)
        etDate.setText(income.date)
        etDescription.setText(income.description)

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
        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            income.amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
            income.category = etCategory.text.toString()
            income.date = etDate.text.toString()
            income.description = etDescription.text.toString()
            filterIncomes()
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteIncome(position: Int) {
        val income = filteredList[position]

        // Create confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Income")
            .setMessage("Are you sure you want to delete this income?")
            .setPositiveButton("OK") { dialog, _ ->
                // Delete income if OK is pressed
                incomeList.remove(income)
                filterIncomes()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Do nothing if Cancel is pressed
                dialog.dismiss()
            }
            .show()
    }

}
