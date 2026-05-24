@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.adapter.CarrinhoAdapter
import com.example.applogintest.model.CarrinhoManager

class CarrinhoActivity : AppCompatActivity() {

    private lateinit var adapter: CarrinhoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrinho)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerCarrinho)
        val tvTotal      = findViewById<TextView>(R.id.tvTotal)
        val tvVazio      = findViewById<TextView>(R.id.tvCarrinhoVazio)
        val btnProsseguir = findViewById<Button>(R.id.btnProsseguir)

        adapter = CarrinhoAdapter(CarrinhoManager.getItens()) { produtoId ->
            CarrinhoManager.remover(produtoId)
            atualizarTela(tvTotal, tvVazio)
            adapter.atualizar(CarrinhoManager.getItens())
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        atualizarTela(tvTotal, tvVazio)

        btnProsseguir.setOnClickListener {
            if (CarrinhoManager.getItens().isEmpty()) {
                Toast.makeText(this, "Seu carrinho está vazio!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, EnderecoActivity::class.java))
        }
    }

    private fun atualizarTela(tvTotal: TextView, tvVazio: TextView) {
        tvTotal.text = "Total: R$ %.2f".format(CarrinhoManager.total())
        tvVazio.visibility = if (CarrinhoManager.getItens().isEmpty()) View.VISIBLE else View.GONE
    }
}
