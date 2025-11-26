package com.example.expensetracker

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    //For emulator
   //private const val BASE_URL = "http://10.0.2.2:5000/"


    //For physical device
    private const val BASE_URL = "http://192.168.0.152:5000/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
