@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.CarrinhoManager
import com.example.applogintest.model.Pedido
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

        val frete        = intent.getDoubleExtra("frete", 0.0)
        val endereco     = intent.getStringExtra("endereco") ?: ""
        val subtotal     = CarrinhoManager.total()
        val total        = subtotal + frete

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

            val formaPagamento = findViewById<RadioButton>(pagamentoId).text.toString()
            val usuarioId      = SessionManager.getToken(this)?.toLongOrNull() ?: 0L
            val nomeUsuario    = SessionManager.getNome(this)
            val emailUsuario   = SessionManager.getEmail(this)

            val itensStr = CarrinhoManager.getItens().joinToString(", ") {
                "${it.produto.nome} x${it.quantidade}"
            }

            val pedido = Pedido(
                usuarioId      = usuarioId,
                nomeUsuario    = nomeUsuario,
                itens          = itensStr,
                subtotal       = subtotal,
                frete          = frete,
                total          = total,
                formaPagamento = formaPagamento,
                endereco       = endereco,
                status         = "pagos",
                statusRastreio = 0
            )

            progressBar.visibility = View.VISIBLE
            btnFinalizar.isEnabled = false

            ApiClient.instance.criarPedido(pedido).enqueue(object : Callback<Pedido> {
                override fun onResponse(call: Call<Pedido>, response: Response<Pedido>) {
                    if (response.isSuccessful) {
                        val pedidoSalvo = response.body()!!

                        // Envia email de confirmação
                        val emailBody = mapOf(
                            "email"          to emailUsuario,
                            "nomeUsuario"    to nomeUsuario,
                            "numeroPedido"   to (pedidoSalvo.id?.toString() ?: "-"),
                            "itens"          to itensStr,
                            "subtotal"       to "%.2f".format(subtotal),
                            "frete"          to "%.2f".format(frete),
                            "total"          to "%.2f".format(total),
                            "formaPagamento" to formaPagamento,
                            "endereco"       to endereco
                        )

                        ApiClient.instance.confirmarEmailPedido(emailBody)
                            .enqueue(object : Callback<Map<String, String>> {
                                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                                    // Email enviado silenciosamente
                                }
                                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                                    // Falha no email não bloqueia o fluxo
                                }
                            })

                        progressBar.visibility = View.GONE
                        btnFinalizar.isEnabled = true

                        Toast.makeText(
                            this@PagamentoActivity,
                            "Pedido finalizado! 🌿 Confirmação enviada para $emailUsuario",
                            Toast.LENGTH_LONG
                        ).show()

                        CarrinhoManager.limpar()
                        finishAffinity()
                        startActivity(Intent(this@PagamentoActivity, HomeActivity::class.java))
                    } else {
                        progressBar.visibility = View.GONE
                        btnFinalizar.isEnabled = true
                        Toast.makeText(this@PagamentoActivity, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Pedido>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnFinalizar.isEnabled = true
                    Toast.makeText(this@PagamentoActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
