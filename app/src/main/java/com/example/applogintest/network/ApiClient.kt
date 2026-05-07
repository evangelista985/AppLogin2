package com.example.applogin.network

import retrofit2.Retrofit
import retrofit2.Retrofit.*
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URL

object ApiClient {
    private val URL = "http://192.168.10.108:8080/"
    val instance: ApiService by lazy{
        val retrofit = Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService ::class.java)

    }
}