@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.*
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PixActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pix)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val total    = intent.getDoubleExtra("total", 0.0)
        val frete    = intent.getDoubleExtra("frete", 0.0)
        val txid     = "PURA${System.currentTimeMillis().toString().takeLast(8)}"
        val chavePix = "puraessenciaetec@gmail.com"

        findViewById<TextView>(R.id.tvValorPix).text  = "R$ %.2f".format(total).replace(".", ",")
        findViewById<TextView>(R.id.tvChavePix).text  = chavePix
        findViewById<TextView>(R.id.tvTxid).text      = "ID da transação: $txid"

        // Timer 30 minutos
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        countDownTimer = object : CountDownTimer(30 * 60 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val min = (millisUntilFinished / 1000) / 60
                val sec = (millisUntilFinished / 1000) % 60
                tvTimer.text = "⏱ Expira em: %02d:%02d".format(min, sec)
                tvTimer.setTextColor(
                    if (millisUntilFinished < 5 * 60 * 1000)
                        android.graphics.Color.parseColor("#dc3545")
                    else
                        getColor(R.color.verde_escuro)
                )
            }
            override fun onFinish() {
                tvTimer.text = "❌ QR Code expirado"
                tvTimer.setTextColor(android.graphics.Color.parseColor("#dc3545"))
                findViewById<Button>(R.id.btnConfirmarPix).isEnabled = false
            }
        }.start()

        // Copiar chave
        findViewById<Button>(R.id.btnCopiarChave).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("chavePix", chavePix))
            Toast.makeText(this, "✅ Chave PIX copiada!", Toast.LENGTH_SHORT).show()
        }

        // Confirmar pagamento
        findViewById<Button>(R.id.btnConfirmarPix).setOnClickListener {
            finalizarPedido(frete, "pix")
        }
    }

    private fun finalizarPedido(frete: Double, formaPagamento: String) {
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarPix)
        btnConfirmar.isEnabled = false
        btnConfirmar.text = "⏳ Processando..."

        val bearerToken  = SessionManager.getBearerToken(this)
        val emailUsuario = SessionManager.getEmail(this)

        val itens = CarrinhoManager.getItens().map { item ->
            ItemPedidoRequest(produto_id = item.produto.id, quantidade = item.quantidade)
        }

        val pedidoRequest = PedidoRequest(
            itens           = itens,
            forma_pagamento = formaPagamento,
            frete           = FreteRequest(valor = frete, nome = "PAC"),
            cupom_codigo    = null
        )

        ApiClient.instance.criarPedido(bearerToken, pedidoRequest)
            .enqueue(object : Callback<PedidoResponse> {
                override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "✅ CONFIRMAR PAGAMENTO"
                    if (response.isSuccessful) {
                        val pedido = response.body()!!
                        Toast.makeText(
                            this@PixActivity,
                            "Pedido #${pedido.pedido_id} finalizado! 🌿\nConfirmação enviada para $emailUsuario",
                            Toast.LENGTH_LONG
                        ).show()
                        CarrinhoManager.limpar()
                        finishAffinity()
                        startActivity(Intent(this@PixActivity, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this@PixActivity, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "✅ CONFIRMAR PAGAMENTO"
                    Toast.makeText(this@PixActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}