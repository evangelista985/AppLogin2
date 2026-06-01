@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.model

object CarrinhoManager {
    private val itens = mutableListOf<CarrinhoItem>()

    fun adicionar(produto: Produto) {
        val existente = itens.find { it.produto.id == produto.id }
        if (existente != null) {
            existente.quantidade++
        } else {
            itens.add(CarrinhoItem(produto))
        }
    }

    fun remover(produtoId: Int) {
        itens.removeAll { it.produto.id == produtoId }
    }

    fun getItens(): List<CarrinhoItem> = itens.toList()

    fun total(): Double = itens.sumOf { it.subtotal }

    fun quantidade(): Int = itens.sumOf { it.quantidade }

    fun limpar() = itens.clear()
}
