package com.example.applogintest.model

data class Pedido(
    val id: Long? = null,
    val usuarioId: Long? = null,
    val nomeUsuario: String? = null,
    val itens: String? = null,
    val subtotal: Double? = null,
    val frete: Double? = null,
    val total: Double? = null,
    val formaPagamento: String? = null,
    val endereco: String? = null,
    val status: String? = "pagos",
    val dataPedido: String? = null,
    val statusRastreio: Int? = 0
)
