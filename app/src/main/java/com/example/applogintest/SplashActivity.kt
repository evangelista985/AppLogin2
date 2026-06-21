@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.Produto
import com.example.applogintest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Tela de abertura exibida assim que o app é aberto.
 *
 * O backend roda no plano gratuito do Render, que "dorme" após um período
 * sem uso e pode levar dezenas de segundos para responder à primeira
 * requisição (cold start). Em vez de o usuário ver esse atraso na tela de
 * produtos (com risco de timeout / "Erro de conexão"), a Splash dispara essa
 * chamada de aquecimento aqui, mostrando a logo da loja enquanto isso.
 *
 * - Se a API responder antes do limite de segurança, segue assim que a
 *   resposta chegar (não fica esperando o tempo todo à toa).
 * - Se não responder a tempo, segue mesmo assim após o limite, para nunca
 *   prender o usuário na splash indefinidamente — a Home já trata produtos
 *   indisponíveis com uma lista de fallback.
 */
class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var jaNavegou = false

    private val TIMEOUT_SEGURANCA_MS = 8000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Limite máximo: se a API não responder a tempo, segue em frente assim mesmo.
        handler.postDelayed({ irParaProximaTela() }, TIMEOUT_SEGURANCA_MS)

        // Aquece o servidor e, se responder antes do limite, já segue na hora.
        ApiClient.instance.listarProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                irParaProximaTela()
            }
            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                // Sem produtos disponíveis agora; a Home usa a lista demo de fallback.
                irParaProximaTela()
            }
        })
    }

    private fun irParaProximaTela() {
        if (jaNavegou) return
        jaNavegou = true
        handler.removeCallbacksAndMessages(null)

        // A Home é sempre a porta de entrada — o cliente navega e vê os produtos
        // livremente sem precisar logar. O login só é pedido quando ele tenta
        // comprar (carrinho, pedidos, admin), conforme já tratado no HomeActivity.
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
