@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.model

// ── Login ─────────────────────────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val senha: String
)

data class LoginResponse(
    val token: String,
    val cliente: ClienteInfo
)

data class ClienteInfo(
    val id: Long,
    val nome: String,
    val email: String,
    val cep: String? = null,
    val endereco: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)

// ── Cadastro ──────────────────────────────────────────────────────────────────
data class CadastroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String? = null,
    val cep: String? = null,
    val endereco: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)

// ── Produto ───────────────────────────────────────────────────────────────────
data class Produto(
    val id: Int = 0,
    val nome: String = "",
    val descricao: String = "",
    val preco: Double = 0.0,
    val imagem: String = "",
    val categoria_id: Int = 0,
    val categoria_nome: String = "",
    val quantidade: Int = 0
) {
    val categoria: String get() = categoria_nome
}

// ── Pedido ────────────────────────────────────────────────────────────────────
data class ItemPedidoRequest(
    val produto_id: Int,
    val quantidade: Int
)

data class FreteRequest(
    val valor: Double,
    val nome: String
)

data class PedidoRequest(
    val itens: List<ItemPedidoRequest>,
    val forma_pagamento: String,
    val frete: FreteRequest? = null,
    val cupom_codigo: String? = null
)

data class PedidoResponse(
    val mensagem: String,
    val pedido_id: Long,
    val total_final: Double
)

data class Pedido(
    val id: Long? = null,
    val cliente_id: Long? = null,
    val total: Double? = null,
    val desconto: Double? = null,
    val total_final: Double? = null,
    val forma_pagamento: String? = null,
    val status: String? = null,
    val criado_em: String? = null,
    val itens_desc: String? = null
) {
    val dataPedido: String? get() = criado_em
    val formaPagamento: String? get() = forma_pagamento
    val statusRastreio: Int? get() = when (status) {
        "pago"      -> 0
        "enviado"   -> 1
        "entregue"  -> 2
        else        -> 0
    }
}


data class AtualizarEnderecoRequest(
    val cep: String,
    val endereco: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val estado: String,
    val complemento: String? = null
)
