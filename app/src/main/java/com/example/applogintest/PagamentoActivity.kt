@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.applogintest.model.*
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PagamentoActivity : AppCompatActivity() {

    private var cartaoVirado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pagamento)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val frete    = intent.getDoubleExtra("frete", 0.0)
        val subtotal = CarrinhoManager.total()
        val total    = subtotal + frete

        findViewById<TextView>(R.id.tvSubtotal).text = "R$ %.2f".format(subtotal)
        findViewById<TextView>(R.id.tvFrete).text    = "R$ %.2f".format(frete)
        findViewById<TextView>(R.id.tvTotal).text    = "R$ %.2f".format(total)

        val rgPagamento     = findViewById<RadioGroup>(R.id.rgPagamento)
        val cardFormCartao  = findViewById<CardView>(R.id.cardFormCartao)
        val btnFinalizar    = findViewById<Button>(R.id.btnFinalizar)
        val progressBar     = findViewById<ProgressBar>(R.id.progressBar)

        val etNumero        = findViewById<EditText>(R.id.etNumeroCartao)
        val etNome          = findViewById<EditText>(R.id.etNomeTitular)
        val etValidade      = findViewById<EditText>(R.id.etValidade)
        val etCvv           = findViewById<EditText>(R.id.etCvv)
        val spinnerParcelas = findViewById<Spinner>(R.id.spinnerParcelas)

        val frameCartao      = findViewById<View>(R.id.frameCartao)
        val cardFrente       = findViewById<View>(R.id.cardFrente)
        val cardVerso        = findViewById<View>(R.id.cardVerso)
        val tvNumeroCartao   = findViewById<TextView>(R.id.tvNumeroCartao)
        val tvNomeTitular    = findViewById<TextView>(R.id.tvNomeTitular)
        val tvValidadeCartao = findViewById<TextView>(R.id.tvValidadeCartao)
        val tvCvvCartao      = findViewById<TextView>(R.id.tvCvvCartao)
        val tvBandeira       = findViewById<TextView>(R.id.tvBandeira)
        val layoutBadge      = findViewById<View>(R.id.layoutBadgeBandeira)
        val tvBandeiraDetect = findViewById<TextView>(R.id.tvBandeiraDetectada)

        // Parcelas
        val parcelasOpcoes = listOf("1x sem juros", "2x sem juros", "3x sem juros")
        spinnerParcelas.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, parcelasOpcoes)

        // Mostrar/ocultar form cartão
        rgPagamento.setOnCheckedChangeListener { _, checkedId ->
            cardFormCartao.visibility = if (checkedId == R.id.rbCartao) View.VISIBLE else View.GONE
        }

        // Flip manual ao tocar no cartão
        frameCartao.setOnClickListener { flipCartao(cardFrente, cardVerso) }

        // ── Número ──
        etNumero.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace(" ", "").replace(Regex("\\D"), "").take(16)
                val formatted = StringBuilder()
                digits.forEachIndexed { i, c ->
                    if (i > 0 && i % 4 == 0) formatted.append(' ')
                    formatted.append(c)
                }
                val result = formatted.toString()
                etNumero.setText(result)
                etNumero.setSelection(result.length)
                isFormatting = false
                tvNumeroCartao.text = result.ifEmpty { "**** **** **** ****" }
                val bandeira = detectarBandeira(digits)
                atualizarBandeira(bandeira, tvBandeira, layoutBadge, tvBandeiraDetect, cardFrente, cardVerso)
            }
        })

        // ── Nome titular ──
        etNome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tvNomeTitular.text = if (s.isNullOrEmpty()) "NOME DO TITULAR" else s.toString().uppercase()
            }
        })

        // ── Validade ──
        etValidade.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("/", "").replace(Regex("\\D"), "")
                val result = when {
                    digits.length >= 3 -> "${digits.take(2)}/${digits.drop(2).take(2)}"
                    digits.length == 2 -> "$digits/"
                    else -> digits
                }.take(5)
                etValidade.setText(result)
                etValidade.setSelection(result.length)
                isFormatting = false
                tvValidadeCartao.text = result.ifEmpty { "MM/AA" }
            }
        })

        // ── CVV ──
        etCvv.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !cartaoVirado) flipCartao(cardFrente, cardVerso)
        }

        etCvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                tvCvvCartao.text = if (s.isNullOrEmpty()) "•••" else "•".repeat(s.length.coerceAtMost(4))
                if ((s?.length ?: 0) >= 3 && cartaoVirado) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (cartaoVirado) flipCartao(cardFrente, cardVerso)
                    }, 600)
                }
            }
        })

        // ── Finalizar ──
        btnFinalizar.setOnClickListener {
            val pagamentoId = rgPagamento.checkedRadioButtonId
            if (pagamentoId == -1) {
                Toast.makeText(this, "Selecione uma forma de pagamento", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (pagamentoId) {
                R.id.rbPix -> {
                    val intent = Intent(this, PixActivity::class.java)
                    intent.putExtra("total", total)
                    intent.putExtra("frete", frete)
                    startActivity(intent)
                }
                R.id.rbBoleto -> {
                    val intent = Intent(this, BoletoActivity::class.java)
                    intent.putExtra("total", total)
                    intent.putExtra("frete", frete)
                    startActivity(intent)
                }
                R.id.rbCartao -> {
                    val numero   = etNumero.text.toString().replace(" ", "")
                    val validade = etValidade.text.toString()
                    val cvv      = etCvv.text.toString()
                    if (numero.length < 13) { Toast.makeText(this, "Número do cartão inválido", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    if (validade.length < 5) { Toast.makeText(this, "Validade inválida", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    if (cvv.length < 3)      { Toast.makeText(this, "CVV inválido", Toast.LENGTH_SHORT).show(); return@setOnClickListener }

                    val bearerToken  = SessionManager.getBearerToken(this)
                    val emailUsuario = SessionManager.getEmail(this)
                    val itens = CarrinhoManager.getItens().map { item ->
                        ItemPedidoRequest(produto_id = item.produto.id, quantidade = item.quantidade)
                    }
                    val pedidoRequest = PedidoRequest(
                        itens           = itens,
                        forma_pagamento = "cartao",
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
                                    val pedido = response.body()!!
                                    Toast.makeText(this@PagamentoActivity,
                                        "Pedido #${pedido.pedido_id} finalizado! 🌿\nConfirmação enviada para $emailUsuario",
                                        Toast.LENGTH_LONG).show()
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
    }

    private fun flipCartao(frente: View, verso: View) {
        val saindo   = if (!cartaoVirado) frente else verso
        val entrando = if (!cartaoVirado) verso  else frente
        val animSaida   = AnimationUtils.loadAnimation(this, R.anim.card_flip_out)
        val animEntrada = AnimationUtils.loadAnimation(this, R.anim.card_flip_in)
        animSaida.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(a: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(a: android.view.animation.Animation?) {}
            override fun onAnimationEnd(a: android.view.animation.Animation?) {
                saindo.visibility   = View.GONE
                entrando.visibility = View.VISIBLE
                entrando.startAnimation(animEntrada)
            }
        })
        saindo.startAnimation(animSaida)
        cartaoVirado = !cartaoVirado
    }

    private fun detectarBandeira(numero: String): String {
        return when {
            numero.startsWith("4") -> "VISA"
            numero.length >= 2 && numero.substring(0, 2).toIntOrNull()?.let { it in 51..55 } == true -> "MASTERCARD"
            numero.length >= 2 && listOf("34", "37").any { numero.startsWith(it) } -> "AMEX"
            numero.length >= 2 && listOf("30", "36", "38").any { numero.startsWith(it) } -> "DINERS"
            numero.length >= 6 && listOf(
                "4011","4312","4389","4514","4576","5067","6277","6363","6504","6516","6550"
            ).any { numero.startsWith(it) } -> "ELO"
            else -> "DEFAULT"
        }
    }

    private fun atualizarBandeira(
        bandeira: String,
        tvBandeira: TextView,
        layoutBadge: View,
        tvDetect: TextView,
        cardFrente: View,
        cardVerso: View
    ) {
        val (simbolo, nome, cor) = when (bandeira) {
            "VISA"       -> Triple("VISA",   "Visa detectado ✓",             "#1a1f71")
            "MASTERCARD" -> Triple("MC",     "Mastercard detectado ✓",       "#eb001b")
            "ELO"        -> Triple("elo",    "Elo detectado ✓",              "#212121")
            "AMEX"       -> Triple("AMEX",   "American Express detectado ✓", "#007bc1")
            "DINERS"     -> Triple("DINERS", "Diners Club detectado ✓",      "#004A97")
            else         -> Triple("",       "",                             "#888888")
        }
        tvBandeira.text = simbolo
        val bgRes = when (bandeira) {
            "VISA"       -> R.drawable.bg_cartao_visa
            "MASTERCARD" -> R.drawable.bg_cartao_mastercard
            "ELO"        -> R.drawable.bg_cartao_elo
            "AMEX"       -> R.drawable.bg_cartao_amex
            "DINERS"     -> R.drawable.bg_cartao_diners
            else         -> R.drawable.bg_cartao_verde
        }
        cardFrente.setBackgroundResource(bgRes)
        cardVerso.setBackgroundResource(bgRes)
        if (bandeira != "DEFAULT" && bandeira.isNotEmpty()) {
            layoutBadge.visibility = View.VISIBLE
            (tvDetect as TextView).apply {
                text = nome
                setTextColor(android.graphics.Color.parseColor(cor))
            }
        } else {
            layoutBadge.visibility = View.GONE
        }
    }
}