package com.example.applogintest.model

data class Produto(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val categoria: String = "",
    val imagemUrl: String = ""
)
