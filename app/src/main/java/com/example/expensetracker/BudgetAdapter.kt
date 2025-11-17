package com.example.expensetracker
data class Budget(
    var year: Int,
    var month: Int, // 1-12
    var amount: Int,
    var usedPercent: Int = 0
)

class BudgetAdapter {
}