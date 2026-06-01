@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.model

data class CarrinhoItem(
    val produto: Produto,
    var quantidade: Int = 1
) {
    val subtotal: Double get() = produto.preco * quantidade
}
