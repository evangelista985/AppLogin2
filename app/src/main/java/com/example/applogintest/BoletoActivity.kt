@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.*
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class BoletoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_boleto)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val total = intent.getDoubleExtra("total", 0.0)
        val frete = intent.getDoubleExtra("frete", 0.0)

        // Gerar dados do boleto
        val nossoNumero   = System.currentTimeMillis().toString().takeLast(10)
        val valorCentavos = (total * 100).toLong().toString().padStart(10, '0')
        val codigoBarras  = "34191.${nossoNumero.take(5)} ${nossoNumero.drop(5)}.123456 78901.234567 8 $valorCentavos"

        // Vencimento: hoje + 3 dias
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 3)
        val dataVenc = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(cal.time)

        findViewById<TextView>(R.id.tvValorBoleto).text   = "R$ %.2f".format(total).replace(".", ",")
        findViewById<TextView>(R.id.tvLinhaDigitavel).text = codigoBarras
        findViewById<TextView>(R.id.tvVencimento).text    = dataVenc
        findViewById<TextView>(R.id.tvNossoNumero).text   = nossoNumero

        // Copiar código
        findViewById<Button>(R.id.btnCopiarBoleto).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("boleto", codigoBarras.replace(" ", "")))
            Toast.makeText(this, "✅ Código copiado!", Toast.LENGTH_SHORT).show()
        }

        // Confirmar pedido
        findViewById<Button>(R.id.btnConfirmarBoleto).setOnClickListener {
            finalizarPedido(frete, "boleto")
        }
    }

    private fun finalizarPedido(frete: Double, formaPagamento: String) {
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarBoleto)
        btnConfirmar.isEnabled = false
        btnConfirmar.text = "⏳ Processando..."

        val bearerToken  = SessionManager.getBearerToken(this)
        val emailUsuario = SessionManager.getEmail(this)

        val itens = CarrinhoManager.getItens().map { item ->
            ItemPedidoRequest(produto_id = item.produto.id, quantidade = item.quantidade)
        }

        // Cupom e endereço vêm da tela de Pagamento (Intent); fallback no SessionManager
        // garante que o pedido não saia sem endereço mesmo se algum extra não chegar.
        val cupomCodigo = intent.getStringExtra("cupom_codigo")
        val enderecoEntrega = EnderecoEntregaRequest(
            cep         = intent.getStringExtra("end_cep").orEmptyToNullBoleto() ?: SessionManager.getCep(this),
            endereco    = intent.getStringExtra("end_logradouro").orEmptyToNullBoleto() ?: SessionManager.getEndereco(this),
            numero      = intent.getStringExtra("end_numero").orEmptyToNullBoleto() ?: SessionManager.getNumero(this),
            complemento = intent.getStringExtra("end_complemento").orEmptyToNullBoleto(),
            bairro      = intent.getStringExtra("end_bairro").orEmptyToNullBoleto() ?: SessionManager.getBairro(this),
            cidade      = intent.getStringExtra("end_cidade").orEmptyToNullBoleto() ?: SessionManager.getCidade(this),
            estado      = intent.getStringExtra("end_estado").orEmptyToNullBoleto() ?: SessionManager.getEstado(this)
        )

        val pedidoRequest = PedidoRequest(
            itens           = itens,
            forma_pagamento = formaPagamento,
            frete           = FreteRequest(valor = frete, nome = "PAC"),
            cupom_codigo    = cupomCodigo,
            endereco_entrega = enderecoEntrega
        )

        ApiClient.instance.criarPedido(bearerToken, pedidoRequest)
            .enqueue(object : Callback<PedidoResponse> {
                override fun onResponse(call: Call<PedidoResponse>, response: Response<PedidoResponse>) {
                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "✅ CONFIRMAR PEDIDO"
                    if (response.isSuccessful) {
                        val pedido = response.body()!!
                        Toast.makeText(
                            this@BoletoActivity,
                            "Pedido #${pedido.pedido_id} finalizado! 🌿\nConfirmação enviada para $emailUsuario",
                            Toast.LENGTH_LONG
                        ).show()
                        CarrinhoManager.limpar()
                        finishAffinity()
                        startActivity(Intent(this@BoletoActivity, HomeActivity::class.java))
                    } else {
                        Toast.makeText(this@BoletoActivity, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PedidoResponse>, t: Throwable) {
                    btnConfirmar.isEnabled = true
                    btnConfirmar.text = "✅ CONFIRMAR PEDIDO"
                    Toast.makeText(this@BoletoActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}

// Converte string vazia em null, para não sobrescrever o fallback do SessionManager.
private fun String?.orEmptyToNullBoleto(): String? = if (this.isNullOrBlank()) null else this
