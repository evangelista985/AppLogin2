package com.example.applogin.network

import com.example.applogin.model.Usuario
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/cadastrar")
    fun criarUsuario(
        @Body user : Usuario
    ): Call<Usuario>
}