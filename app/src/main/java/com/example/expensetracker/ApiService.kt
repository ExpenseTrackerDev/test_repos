package com.example.expensetracker

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

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


data class DashboardResponse(
    val username: String,
    val email: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val recentTransactions: List<Transaction>,
    val dailyIncome: List<Double>,
    val dailyExpense: List<Double>,
    val advice: String
)

//data class Transaction(
//    val date: String,
//    val type: String,
//    val amount: Double,
//    val category: String,
//    val description: String
//)



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



    @GET("dashboard/data")
    fun getDashboardData(
        @Query("userId") userId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Call<DashboardResponse>


}
