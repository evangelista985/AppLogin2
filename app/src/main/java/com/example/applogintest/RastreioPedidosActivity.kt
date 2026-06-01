@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.adapter.PedidoAdapter
import com.example.applogintest.model.Pedido
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RastreioPedidosActivity : AppCompatActivity() {

    private lateinit var adapter: PedidoAdapter
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rastreio_pedidos)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val recycler    = findViewById<RecyclerView>(R.id.recyclerPedidos)
        val tvVazio     = findViewById<TextView>(R.id.tvVazio)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        adapter = PedidoAdapter(emptyList())
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        carregarPedidos(tvVazio, progressBar)

        handler.postDelayed(object : Runnable {
            override fun run() {
                carregarPedidos(tvVazio, progressBar)
                handler.postDelayed(this, 30000)
            }
        }, 30000)
    }

    private fun carregarPedidos(tvVazio: TextView, progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        val token = SessionManager.getBearerToken(this)

        ApiClient.instance.listarPedidos(token)
            .enqueue(object : Callback<List<Pedido>> {
                override fun onResponse(call: Call<List<Pedido>>, response: Response<List<Pedido>>) {
                    progressBar.visibility = View.GONE
                    val lista = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                    adapter.atualizar(lista)
                    tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onFailure(call: Call<List<Pedido>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@RastreioPedidosActivity, "Falha ao carregar pedidos", Toast.LENGTH_SHORT).show()
                    tvVazio.visibility = View.VISIBLE
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        adapter.limparHandlers()
    }
}
