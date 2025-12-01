package com.example.expensetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*



class IncomeActivity : AppCompatActivity() {

    private lateinit var recyclerIncome: RecyclerView
    private lateinit var incomeAdapter: IncomeAdapter
    private val incomeList = mutableListOf<Income>()
    private val filteredList = mutableListOf<Income>()

    private lateinit var spinnerMonth: Spinner
    private lateinit var spinnerYear: Spinner
    private lateinit var etSearchCategory: EditText
    private lateinit var etFromDate: EditText

    private lateinit var userId: String

    private fun formatDate(rawDate: String): String {
        return try {
            rawDate.substring(0, 10)
        } catch (e: Exception) {
            rawDate
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_income)

        val prefs = getSharedPreferences("userPrefs", MODE_PRIVATE)
        userId = prefs.getString("userId", "") ?: ""

        recyclerIncome = findViewById(R.id.recyclerIncome)
        recyclerIncome.layoutManager = LinearLayoutManager(this)
        incomeAdapter = IncomeAdapter(filteredList, ::editIncome, ::deleteIncome)
        recyclerIncome.adapter = incomeAdapter

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
                (2023..Calendar.getInstance().get(Calendar.YEAR))
                    .indexOf(Calendar.getInstance().get(Calendar.YEAR))
            )
            filterIncomes()
        }

        findViewById<FloatingActionButton>(R.id.btnAddIncome).setOnClickListener {
            showAddIncomeDialog()
        }

        setupMonthYearSpinners()
        setupCategorySearch()
        setupDatePicker()
    }

    override fun onResume() {
        super.onResume()
        loadIncomesFromBackend()
    }

    private fun toggleActionButtons() {
        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
        val selectedYear = spinnerYear.selectedItem.toString().toInt()

        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        val isCurrentMonth = (selectedMonth == currentMonth && selectedYear == currentYear)

        findViewById<FloatingActionButton>(R.id.btnAddIncome).visibility =
            if (isCurrentMonth) View.VISIBLE else View.GONE

        incomeAdapter.showButtons = isCurrentMonth
        incomeAdapter.notifyDataSetChanged()
    }

    private fun loadIncomesFromBackend() {
        RetrofitClient.instance.getIncomes(userId)
            .enqueue(object : Callback<List<IncomeResponse>> {
                override fun onResponse(
                    call: Call<List<IncomeResponse>>,
                    response: Response<List<IncomeResponse>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        incomeList.clear()
                        incomeList.addAll(response.body()!!.map { inc ->
                            Income(
                                id = inc.id,
                                category = inc.category,
                                amount = inc.amount,
                                date = inc.date.substringBefore("T"),
                                description = inc.description
                            )
                        })
                        filterIncomes()
                    } else {
                        Log.e("IncomeActivity", "API Error: ${response.code()} ${response.errorBody()?.string()}")
                        Toast.makeText(this@IncomeActivity, "Failed to load incomes", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<IncomeResponse>>, t: Throwable) {
                    Log.e("IncomeActivity", "API Failure", t)
                    Toast.makeText(this@IncomeActivity, "Failed to load incomes", Toast.LENGTH_SHORT).show()
                }
            })
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterIncomes()
                toggleActionButtons()
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

    private fun setupDatePicker() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etFromDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val c = Calendar.getInstance()
                    c.set(year, month, dayOfMonth)
                    etFromDate.setText(format.format(c.time))
                    filterIncomes()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun filterIncomes() {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fromDate = if (etFromDate.text.isNotEmpty()) {
            try { format.parse(etFromDate.text.toString().trim()) } catch (e: Exception) { null }
        } else null

        val categoryQuery = etSearchCategory.text.toString().trim().lowercase()
        val selectedMonth = spinnerMonth.selectedItem.toString().toInt()
        val selectedYear = spinnerYear.selectedItem.toString().toInt()

        filteredList.clear()
        for (income in incomeList) {
            val incomeDate = try { format.parse(income.date.trim()) } catch (e: Exception) { null } ?: continue
            val matchesCategory = categoryQuery.isEmpty() || income.category.lowercase().contains(categoryQuery)
            val matchesDate = fromDate == null || incomeDate.time == fromDate.time
            val cal = Calendar.getInstance()
            cal.time = incomeDate
            val matchesMonthYear = cal.get(Calendar.MONTH) + 1 == selectedMonth && cal.get(Calendar.YEAR) == selectedYear

            if (matchesCategory && matchesDate && matchesMonthYear) {
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

        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amount == null || amount <= 0 || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.addIncome(userId, IncomeRequest(category, amount, date, description))
                .enqueue(object : Callback<IncomeResponse> {
                    override fun onResponse(call: Call<IncomeResponse>, response: Response<IncomeResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            Toast.makeText(this@IncomeActivity, "Income added", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            loadIncomesFromBackend()
                        } else {
                            Log.e("IncomeActivity", "Add Income Error: ${response.code()} ${response.errorBody()?.string()}")
                            Toast.makeText(this@IncomeActivity, "Failed to add income", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<IncomeResponse>, t: Throwable) {
                        Log.e("IncomeActivity", "Add Income Failure", t)
                        Toast.makeText(this@IncomeActivity, "Failed to add income", Toast.LENGTH_SHORT).show()
                    }
                })
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

        dialogView.findViewById<Button>(R.id.btnSaveIncome).setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            val category = etCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amount == null || amount <= 0 || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val incomeId = income.id ?: run {
                Toast.makeText(this, "Income ID missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            RetrofitClient.instance.editIncome(userId, incomeId, IncomeRequest(category, amount, date, description))
                .enqueue(object : Callback<IncomeResponse> {
                    override fun onResponse(call: Call<IncomeResponse>, response: Response<IncomeResponse>) {
                        if (response.isSuccessful && response.body() != null) {
                            val updated = response.body()!!
                            income.category = updated.category
                            income.amount = updated.amount
                            income.date = updated.date
                            income.description = updated.description

                            filterIncomes()
                            dialog.dismiss()
                            Toast.makeText(this@IncomeActivity, "Income updated", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("IncomeEdit", "Error code=${response.code()}, err=${response.errorBody()?.string()}")
                            Toast.makeText(this@IncomeActivity, "Unable to update income", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<IncomeResponse>, t: Throwable) {
                        Log.e("IncomeEdit", "Network failure: ${t.message}", t)
                        Toast.makeText(this@IncomeActivity, "Failed to update income", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        dialog.show()
    }

    private fun deleteIncome(position: Int) {
        val income = filteredList[position]

        AlertDialog.Builder(this)
            .setTitle("Delete Income")
            .setMessage("Are you sure you want to delete this income?")
            .setPositiveButton("OK") { dialog, _ ->
                val incomeId = income.id
                if (incomeId == null) {
                    Toast.makeText(this, "Income ID missing", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    return@setPositiveButton
                }

                RetrofitClient.instance.deleteIncome(userId, incomeId)
                    .enqueue(object : Callback<ApiResponse> {
                        override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@IncomeActivity, "Income deleted", Toast.LENGTH_SHORT).show()
                                loadIncomesFromBackend()
                            } else {
                                Log.e("IncomeDelete", "Delete failed code=${response.code()}, err=${response.errorBody()?.string()}")
                                Toast.makeText(this@IncomeActivity, "Unable to delete income", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                            Log.e("IncomeDelete", "Network failure: ${t.message}", t)
                            Toast.makeText(this@IncomeActivity, "Failed to delete income", Toast.LENGTH_SHORT).show()
                        }
                    })

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
