package com.example.expensetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import android.view.View
import java.util.*

data class Expense(
    val id: String?,
    var category: String,
    var amount: Double,
    var date: String,
    var description: String
)

class ExpenseActivity : AppCompatActivity() {

    private lateinit var recyclerExpense: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<Expense>()
    private val filteredList = mutableListOf<Expense>()

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var etSearchCategory: EditText
    private lateinit var etFromDate: EditText

    private lateinit var userId: String

    private fun formatDate(rawDate: String): String {
        return try {
            rawDate.substring(0, 10)   // "2025-11-21"
        } catch (e: Exception) {
            rawDate                     // fallback
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense)

        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""

        recyclerExpense = findViewById(R.id.recyclerExpense)
        recyclerExpense.layoutManager = LinearLayoutManager(this)
        expenseAdapter = ExpenseAdapter(filteredList, ::editExpense, ::deleteExpense)
        recyclerExpense.adapter = expenseAdapter

        spinnerMonth = findViewById(R.id.spinnerMonth)
        spinnerYear = findViewById(R.id.spinnerYear)
        etSearchCategory = findViewById(R.id.etSearchCategory)
        etFromDate = findViewById(R.id.etFromDate)

        findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnClearFilter).setOnClickListener {
            etFromDate.text.clear()
            etSearchCategory.text.clear()
            spinnerMonth.setSelection(Calendar.getInstance().get(Calendar.MONTH))
            spinnerYear.setSelection(
                (2023..Calendar.getInstance().get(Calendar.YEAR)).indexOf(Calendar.getInstance().get(Calendar.YEAR))
            )
            filterExpenses()
        }

        findViewById<FloatingActionButton>(R.id.btnAddExpense).setOnClickListener {
            showAddExpenseDialog()
        }


        setupMonthYearSpinners()
        setupCategorySearch()
        setupDatePicker()
    }

    override fun onResume() {
        super.onResume()
        loadExpensesFromBackend()
    }

    private fun toggleActionButtons() {
        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
        val selectedYear = spinnerYear.selectedItem.toString().toInt()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
        val currentYear = calendar.get(Calendar.YEAR)

        val isCurrentMonth = (selectedMonth == currentMonth && selectedYear == currentYear)

        // Floating Add Button
        findViewById<FloatingActionButton>(R.id.btnAddExpense).visibility =
            if (isCurrentMonth) View.VISIBLE else View.GONE

        // RecyclerView Buttons: hide/show edit & delete
        expenseAdapter.showButtons = isCurrentMonth
        expenseAdapter.notifyDataSetChanged()
    }

    private fun loadExpensesFromBackend() {
        RetrofitClient.instance.getExpenses(userId)
            .enqueue(object : Callback<List<ExpenseResponse>> {
                override fun onResponse(
                    call: Call<List<ExpenseResponse>>,
                    response: Response<List<ExpenseResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        expenseList.clear()
                        expenseList.addAll(response.body()!!.map { exp ->
                            Expense(
                                id = exp.id,  // already mapped from _id
                                category = exp.category,
                                amount = exp.amount,
                                date = exp.date.substringBefore("T"), // format to yyyy-MM-dd
                                description = exp.description
                            )
                        })
                        filterExpenses()
                    } else {
                        Log.e("ExpenseActivity", "API Error: ${response.code()} ${response.errorBody()?.string()}")
                        Toast.makeText(this@ExpenseActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                    Log.e("ExpenseActivity", "API Failure", t)
                    Toast.makeText(this@ExpenseActivity, "Failed to load expenses", Toast.LENGTH_SHORT).show()
                }
            })
    }



//    private fun setupMonthYearSpinners() {
//        val months = (1..12).map { it.toString() }
//        val years = (2023..Calendar.getInstance().get(Calendar.YEAR)).map { it.toString() }
//
//        spinnerMonth.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, months)
//        spinnerYear.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, years)
//
//        val cal = Calendar.getInstance()
//        spinnerMonth.setSelection(cal.get(Calendar.MONTH))
//        spinnerYear.setSelection(years.indexOf(cal.get(Calendar.YEAR).toString()))
//
//        val listener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
//                filterExpenses()
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }
//
//        spinnerMonth.onItemSelectedListener = listener
//        spinnerYear.onItemSelectedListener = listener
//    }

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
                toggleActionButtons()
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

    private fun setupDatePicker() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etFromDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    etFromDate.setText(format.format(c.time))
                    filterExpenses()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun filterExpenses() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDate = if (etFromDate.text.isNotEmpty()) {
            try { format.parse(etFromDate.text.toString().trim()) } catch (e: Exception) { null }
        } else null

        val categoryQuery = etSearchCategory.text.toString().trim().lowercase()
        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
        val selectedYear = spinnerYear.selectedItem.toString().toInt()

        filteredList.clear()
        for (expense in expenseList) {
            val expenseDate = try { format.parse(expense.date.trim()) } catch (e: Exception) { null } ?: continue
            val matchesCategory = categoryQuery.isEmpty() || expense.category.lowercase().contains(categoryQuery)
            val matchesDate = fromDate == null || expenseDate.time == fromDate.time
            val cal = Calendar.getInstance()
            cal.time = expenseDate
            val matchesMonthYear = cal.get(Calendar.MONTH) + 1 == selectedMonth && cal.get(Calendar.YEAR) == selectedYear

            if (matchesCategory && matchesDate && matchesMonthYear) {
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

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    etDate.setText(format.format(c.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amount == null || amount <= 0 || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.addExpense(userId, ExpenseRequest(category, amount, date, description))
                .enqueue(object : Callback<ExpenseResponse> {
                    override fun onResponse(call: Call<ExpenseResponse>, response: Response<ExpenseResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@ExpenseActivity, "Expense added", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadExpensesFromBackend()
                        } else {
                            Log.e("ExpenseActivity", "Add Expense Error: ${response.code()} ${response.errorBody()?.string()}")
                            Toast.makeText(this@ExpenseActivity, "Failed to add expense", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ExpenseResponse>, t: Throwable) {
                        Log.e("ExpenseActivity", "Add Expense Failure", t)
                        Toast.makeText(this@ExpenseActivity, "Failed to add expense", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        dialog.show()
    }

//    private fun editExpense(position: Int) {
//        val expense = filteredList[position]
//        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
//        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
//        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
//        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
//        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)
//
//        etAmount.setText(expense.amount.toString())
//        etCategory.setText(expense.category)
//        etDate.setText(expense.date)
//        etDescription.setText(expense.description)
//
//        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        etDate.setOnClickListener {
//            val cal = Calendar.getInstance()
//            DatePickerDialog(this,
//                { _, year, month, dayOfMonth ->
//                    val c = Calendar.getInstance()
//                    c.set(year, month, dayOfMonth)
//                    etDate.setText(format.format(c.time))
//                },
//                cal.get(Calendar.YEAR),
//                cal.get(Calendar.MONTH),
//                cal.get(Calendar.DAY_OF_MONTH)
//            ).show()
//        }
//
//        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
//
//        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
//            val amount = etAmount.text.toString().toDoubleOrNull()
//            val category = etCategory.text.toString().trim()
//            val date = etDate.text.toString().trim()
//            val description = etDescription.text.toString().trim()
//
//            if (amount == null || amount <= 0 || category.isEmpty() || date.isEmpty()) {
//                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            RetrofitClient.instance.editExpense(userId, expense.id, ExpenseRequest(category, amount, date, description))
//                .enqueue(object : Callback<ExpenseResponse> {
//                    override fun onResponse(call: Call<ExpenseResponse>, response: Response<ExpenseResponse>) {
//                        if (response.isSuccessful && response.body() != null) {
//                            Toast.makeText(this@ExpenseActivity, "Expense updated", Toast.LENGTH_SHORT).show()
//                            dialog.dismiss()
//                            loadExpensesFromBackend()
//                        } else {
//                            Log.e("ExpenseActivity", "Edit Expense Error: ${response.code()} ${response.errorBody()?.string()}")
//                            Toast.makeText(this@ExpenseActivity, "Failed to update expense", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(call: Call<ExpenseResponse>, t: Throwable) {
//                        Log.e("ExpenseActivity", "Edit Expense Failure", t)
//                        Toast.makeText(this@ExpenseActivity, "Failed to update expense", Toast.LENGTH_SHORT).show()
//                    }
//                })
//        }
//
//        dialog.show()
//    }


    private fun editExpense(position: Int) {
        val expense = filteredList[position]

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = dialogView.findViewById<EditText>(R.id.etExpenseCategory)
        val etDate = dialogView.findViewById<EditText>(R.id.etExpenseDate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etExpenseDescription)

        // set old values
        etAmount.setText(expense.amount.toString())
        etCategory.setText(expense.category)
//        etDate.setText(expense.date)
        etDate.setText(expense.date.substringBefore("T"))
        etDescription.setText(expense.description)

        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dialog = DatePickerDialog(
                this,
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
            dialog.show()
        }

        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.btnSaveExpense).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amount == null || amount <= 0 || category.isBlank() || date.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expenseId = expense.id ?: run {
                Toast.makeText(this, "Expense ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.editExpense(
                userId,
                expenseId,
                ExpenseRequest(category, amount, date, description)
            ).enqueue(object : Callback<ExpenseResponse> {
                override fun onResponse(
                    call: Call<ExpenseResponse>,
                    response: Response<ExpenseResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val updated = response.body()!!

                        // update local item
                        expense.category = updated.category
                        expense.amount = updated.amount
                        expense.date = updated.date
                        expense.description = updated.description

                        filterExpenses()  // refresh list
                        alertDialog.dismiss()

                        Toast.makeText(
                            this@ExpenseActivity,
                            "Expense updated",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(
                            "ExpenseEdit",
                            "Error code=${response.code()}, err=${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            this@ExpenseActivity,
                            "Unable to update expense",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ExpenseResponse>, t: Throwable) {
                    Log.e("ExpenseEdit", "Network failure: ${t.message}", t)
                    Toast.makeText(
                        this@ExpenseActivity,
                        "Failed to update expense",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        alertDialog.show()
    }




//    private fun deleteExpense(position: Int) {
//        val expense = filteredList[position]
//        AlertDialog.Builder(this)
//            .setTitle("Delete Expense")
//            .setMessage("Are you sure you want to delete this expense?")
//            .setPositiveButton("OK") { dialog, _ ->
//                RetrofitClient.instance.deleteExpense(userId, expense.id)
//                    .enqueue(object : Callback<ApiResponse> {
//                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                            if (response.isSuccessful) {
//                                Toast.makeText(this@ExpenseActivity, "Expense deleted", Toast.LENGTH_SHORT).show()
//                                loadExpensesFromBackend()
//                            } else {
//                                Log.e("ExpenseActivity", "Delete Expense Error: ${response.code()} ${response.errorBody()?.string()}")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                            Log.e("ExpenseActivity", "Delete Expense Failure", t)
//                            Toast.makeText(this@ExpenseActivity, "Failed to delete expense", Toast.LENGTH_SHORT).show()
//                        }
//                    })
//                dialog.dismiss()
//            }
//            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
//            .show()
//    }

    private fun deleteExpense(position: Int) {
        val expense = filteredList[position]

        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("OK") { dialog, _ ->

                // Expense ID (returned from backend)
                val expenseId = expense.id
                if (expenseId == null) {
                    Toast.makeText(this, "Expense ID missing", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                RetrofitClient.instance.deleteExpense(userId, expenseId)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(
                            call: Call<ApiResponse>,
                            response: Response<ApiResponse>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(
                                    this@ExpenseActivity,
                                    "Expense deleted",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Reload full list to update UI
                                loadExpensesFromBackend()
                            } else {
                                Log.e(
                                    "ExpenseDelete",
                                    "Delete failed code=${response.code()}, err=${response.errorBody()?.string()}"
                                )
                                Toast.makeText(
                                    this@ExpenseActivity,
                                    "Unable to delete expense",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Log.e("ExpenseDelete", "Network failure: ${t.message}", t)
                            Toast.makeText(
                                this@ExpenseActivity,
                                "Failed to delete expense",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }


}
