package com.example.expensetracker.model

//import com.google.gson.annotations.SerializedName
//
//data class DashboardResponse(
//    @SerializedName("username") val username: String?,
//    @SerializedName("email") val email: String?,
//    @SerializedName("total_income") val totalIncome: Double?,
//    @SerializedName("total_expense") val totalExpense: Double?,
//    @SerializedName("balance") val balance: Double?,
//    @SerializedName("advice") val advice: String?,
//    @SerializedName("recent_transactions") val recentTransactions: List<Transaction>?,
//    @SerializedName("daily_income") val dailyIncome: List<Double>?,
//    @SerializedName("daily_expense") val dailyExpense: List<Double>?
//)
//data class Transaction(
//    @SerializedName("category") val category: String?,
//    @SerializedName("amount") val amount: Double?,
//    @SerializedName("date") val date: String?,
//    @SerializedName("type") val type: String?
//)
data class DashboardResponse(
    val username: String?,
    val email: String?,
    val totalIncome: Double?,
    val totalExpense: Double?,
    val balance: Double?,
    val advice: String?,
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
