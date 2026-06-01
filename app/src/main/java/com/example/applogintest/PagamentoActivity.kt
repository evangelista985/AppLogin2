@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.*
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PagamentoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val frete    = intent.getDoubleExtra("frete", 0.0)
        val endereco = intent.getStringExtra("endereco") ?: ""
        val subtotal = CarrinhoManager.total()
        val total    = subtotal + frete

        val tvSubtotal   = findViewById<TextView>(R.id.tvSubtotal)
        val tvFrete      = findViewById<TextView>(R.id.tvFrete)
        val tvTotal      = findViewById<TextView>(R.id.tvTotal)
        val rgPagamento  = findViewById<RadioGroup>(R.id.rgPagamento)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)
        val progressBar  = findViewById<ProgressBar>(R.id.progressBar)

        tvSubtotal.text = "R$ %.2f".format(subtotal)
        tvFrete.text    = "R$ %.2f".format(frete)
        tvTotal.text    = "R$ %.2f".format(total)

        btnFinalizar.setOnClickListener {
            val pagamentoId = rgPagamento.checkedRadioButtonId
            if (pagamentoId == -1) {
                Toast.makeText(this, "Selecione uma forma de pagamento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formaPagamento = when (pagamentoId) {
                R.id.rbPix    -> "pix"
                R.id.rbCartao -> "cartao"
                R.id.rbBoleto -> "boleto"
                else          -> "pix"
            }

            val bearerToken = SessionManager.getBearerToken(this)
            val nomeUsuario = SessionManager.getNome(this)
            val emailUsuario = SessionManager.getEmail(this)

            // Monta itens no formato que o Node espera
            val itens = CarrinhoManager.getItens().map { item ->
                ItemPedidoRequest(
                    produto_id = item.produto.id,
                    quantidade = item.quantidade
                )
            }

            val pedidoRequest = PedidoRequest(
                itens           = itens,
                forma_pagamento = formaPagamento,
                frete           = FreteRequest(valor = frete, nome = "PAC"),
                cupom_codigo    = null
            )

            progressBar.visibility = View.VISIBLE
            btnFinalizar.isEnabled = false

            ApiClient.instance.criarPedido(bearerToken, pedidoRequest)
                .enqueue(object : Callback<PedidoResponse> {
                    override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                        progressBar.visibility = View.GONE
                        btnFinalizar.isEnabled = true
                        if (response.isSuccessful) {
                            val pedidoSalvo = response.body()!!
                            Toast.makeText(
                                this@PagamentoActivity,
                                "Pedido #${pedidoSalvo.pedido_id} finalizado! 🌿\nConfirmação enviada para $emailUsuario",
                                Toast.LENGTH_LONG
                            ).show()
                            CarrinhoManager.limpar()
                            finishAffinity()
                            startActivity(Intent(this@PagamentoActivity, HomeActivity::class.java))
                        } else {
                            Toast.makeText(this@PagamentoActivity, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnFinalizar.isEnabled = true
                        Toast.makeText(this@PagamentoActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
