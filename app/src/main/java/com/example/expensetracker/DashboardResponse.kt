package com.example.expensetracker.model

data class DashboardResponse(
    val username: String?,
    val email: String?,
    val totalIncome: Double?,
    val totalExpense: Double?,
    val balance: Double?,
    val advice: String?,                  // general advice
    val categoryAdvice: String?,          // advice about high-spending category
    val incomeUsageMessage: String?,      // percentage of income used
    val hasPreviousData: Boolean?,        // true if previous month data exists
    val recentTransactions: List<Transaction>?,
    val dailyIncome: List<Double>?,
    val dailyExpense: List<Double>?
)


data class Transaction(
    val type: String?,
    val amount: Double?,
    val category: String?,
    val description: String?,
    val date: String?
)
