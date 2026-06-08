@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Troque pelo IPv4 da sua máquina (ipconfig no terminal)
    const val BASE_URL = "http://10.0.10.136:3001"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}