package com.example.expensetracker
import okhttp3.ResponseBody
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.Path
import com.example.expensetracker.model.DashboardResponse


data class RegisterRequest(
    val username: String,
    val email: String,
    val phone: String,
    val password: String,
    val confirmPassword: String
)

data class VerifyRequest(
    val email: String,
    val otp: String
)

data class ApiResponse(
    val message: String
)
data class ResendOtpRequest(
    val email: String
)
data class ResetRequest(
    val email: String, val newPassword: String, val confirmNewPassword: String
)
data class ResetVerifyRequest(
    val email: String, val otp: String
)
data class LoginRequest(
    val identifier: String,   // email OR username
    val password: String
)
data class LoginResponse(
    val message: String,
    val userId: String,
    val username: String,
    val email: String
)



//
//data class DashboardResponse(
//    val username: String,
//    val email: String,
//    val totalIncome: Double,
//    val totalExpense: Double,
//    val balance: Double,
//    val recentTransactions: List<Transaction>,
//    val dailyIncome: List<Double>,
//    val dailyExpense: List<Double>,
//    val advice: String
//)


data class ExpenseRequest(
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
)

data class ExpenseResponse(
    @SerializedName("_id")
    val id: String,
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
)


data class BudgetResponse(
    @SerializedName("_id")
    val budgetId: String = "",
    val month: Int = 0,
    val year: Int = 0,
    val amount: Int = 0,
    val usedAmount: Int = 0,
    val percentUsed: Int = 0,
    val editable: Boolean = true,
    val overBudget: Int = 0
)

data class AddBudgetRequest(
    val month: Int,
    val year: Int,
    val amount: Int
)

data class EditBudgetRequest(
    val amount: Int
)
data class IncomeRequest(
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
)

data class IncomeResponse(
    @SerializedName("_id")
    val id: String,
    val category: String,
    val amount: Double,
    val date: String,
    val description: String
)

interface ApiService {

    @POST("api/auth/register")
    fun registerUser(@Body request: RegisterRequest): Call<ApiResponse>

    @POST("api/auth/verify-email")
    fun verifyEmail(@Body request: VerifyRequest): Call<ApiResponse>

    @POST("api/auth/resend-otp")
    fun resendOtp(@Body request: ResendOtpRequest): Call<ApiResponse>

    @POST("api/auth/reset-request")
    fun resetRequest(@Body request: ResetRequest): Call<ApiResponse>

    @POST("api/auth/reset-verify")
    fun resetVerify(@Body request: ResetVerifyRequest): Call<ApiResponse>

    @POST("api/auth/login")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>



    @GET("api/dashboard/data")
    fun getDashboardData(
        @Query("userId") userId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Call<DashboardResponse>

    @POST("api/expenses/add")
    fun addExpense(@Query("userId") userId: String, @Body expense: ExpenseRequest): Call<ExpenseResponse>

    @POST("api/expenses/edit")
    fun editExpense(@Query("userId") userId: String, @Query("expenseId") expenseId: String?, @Body expense: ExpenseRequest): Call<ExpenseResponse>

    @POST("api/expenses/delete")
    fun deleteExpense(@Query("userId") userId: String, @Query("expenseId") expenseId: String?): Call<ApiResponse>

    @GET("api/expenses/data")
    fun getExpenses(@Query("userId") userId: String): Call<List<ExpenseResponse>>

    @POST("api/budget/add")
    fun addBudget(
        @Body request: AddBudgetRequest,
        @Query("userId") userId: String
    ): Call<BudgetResponse>

    // Edit Budget
    @POST("api/budget/edit/{budgetId}")
    fun editBudget(
        @Path("budgetId") budgetId: String,
        @Body request: EditBudgetRequest,
        @Query("userId") userId: String
    ): Call<BudgetResponse>

    // Get Budgets for a year
    @GET("api/budget/{year}")
    fun getBudgets(
        @Path("year") year: Int,
        @Query("userId") userId: String
    ): Call<List<BudgetResponse>>

    @GET("api/budget/all/{userId}")
    fun getAllBudgets(@Path("userId") userId: String): Call<List<BudgetResponse>>

    @POST("api/incomes/add")
    fun addIncome(@Query("userId") userId: String, @Body income: IncomeRequest): Call<IncomeResponse>

    @POST("api/incomes/edit")
    fun editIncome(@Query("userId") userId: String, @Query("incomeId") incomeId: String?, @Body income: IncomeRequest): Call<IncomeResponse>

    @POST("api/incomes/delete")
    fun deleteIncome(@Query("userId") userId: String, @Query("incomeId") incomeId: String?): Call<ApiResponse>

    @GET("api/incomes/data")
    fun getIncomes(@Query("userId") userId: String): Call<List<IncomeResponse>>

    @GET("api/incomes/filter")
    fun filterIncomes(@Query("userId") userId: String, @Query("category") category: String?, @Query("date") date: String?): Call<List<IncomeResponse>>

    @GET("api/report/pdf")
    fun getPdfReport(
        @Query("userId") userId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Call<ResponseBody>

}
