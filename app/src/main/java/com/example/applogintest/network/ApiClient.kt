@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    const val BASE_URL = "https://projpuraessenciareact.onrender.com"

    // O backend roda no plano gratuito do Render, que "dorme" após um tempo
    // sem uso e pode levar até ~60s para responder a primeira requisição
    // após esse período de inatividade (cold start). Os timeouts padrão do
    // OkHttp (10s) são curtos demais para isso e geram falso "Erro de conexão"
    // mesmo com a internet do usuário funcionando normalmente.
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
