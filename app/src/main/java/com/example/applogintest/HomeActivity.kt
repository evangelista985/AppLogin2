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
import com.example.applogintest.adapter.ProdutoAdapter
import com.example.applogintest.model.CarrinhoManager
import com.example.applogintest.model.Produto
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var adapter: ProdutoAdapter
    private var todosProdutos = listOf<Produto>()

    private val produtosDemo = listOf(
        Produto(1, "Chá Camomila", "Relaxante e calmante, ideal para o sono", 12.90, "chas"),
        Produto(2, "Chá Verde", "Rico em antioxidantes, estimulante natural", 14.90, "chas"),
        Produto(3, "Alecrim", "Tempero aromático para carnes e massas", 8.50, "temperos"),
        Produto(4, "Cúrcuma", "Anti-inflamatório natural, cor vibrante", 18.90, "temperos"),
        Produto(5, "Lavanda", "Erva para relaxamento e aromaterapia", 22.00, "ervas"),
        Produto(6, "Hortelã", "Digestiva e refrescante", 9.90, "ervas"),
        Produto(7, "Creme Hidratante Natural", "Base de aloe vera e manteiga de karité", 45.00, "cosmeticos"),
        Produto(8, "Óleo de Argan", "Nutritivo para cabelos e pele", 68.00, "cosmeticos")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val tvSaudacao     = findViewById<TextView>(R.id.tvSaudacao)
        val btnCarrinho    = findViewById<ImageButton>(R.id.btnCarrinho)
        val btnAdmin       = findViewById<ImageButton>(R.id.btnAdmin)
        val btnPedidos     = findViewById<ImageButton>(R.id.btnPedidos)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val chipTodos      = findViewById<Button>(R.id.chipTodos)
        val chipChas       = findViewById<Button>(R.id.chipChas)
        val chipTemperos   = findViewById<Button>(R.id.chipTemperos)
        val chipErvas      = findViewById<Button>(R.id.chipErvas)
        val chipCosmeticos = findViewById<Button>(R.id.chipCosmeticos)
        val recyclerView   = findViewById<RecyclerView>(R.id.recyclerProdutos)
        val progressBar    = findViewById<ProgressBar>(R.id.progressBar)
        val tvCarrinhoQtd  = findViewById<TextView>(R.id.tvCarrinhoQtd)

        val logado = SessionManager.estaLogado(this)

        if (logado) {
            tvSaudacao.text = getString(R.string.saudacao, SessionManager.getNome(this))
            btnCarrinho.visibility = View.VISIBLE
            btnAdmin.visibility    = View.VISIBLE
            btnPedidos.visibility  = View.VISIBLE
            btnLogout.visibility   = View.VISIBLE
        } else {
            tvSaudacao.text = getString(R.string.boas_vindas_catalogo)
            btnCarrinho.visibility   = View.GONE
            btnAdmin.visibility      = View.GONE
            btnPedidos.visibility    = View.GONE
            btnLogout.visibility     = View.GONE
            tvCarrinhoQtd.visibility = View.GONE
        }

        adapter = ProdutoAdapter(emptyList(), logado) { produto ->
            Toast.makeText(this, getString(R.string.produto_adicionado, produto.nome), Toast.LENGTH_SHORT).show()
            tvCarrinhoQtd.text = CarrinhoManager.quantidade().toString()
            tvCarrinhoQtd.visibility = if (CarrinhoManager.quantidade() > 0) View.VISIBLE else View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        chipTodos.setOnClickListener      { filtrar("todos") }
        chipChas.setOnClickListener       { filtrar("chas") }
        chipTemperos.setOnClickListener   { filtrar("temperos") }
        chipErvas.setOnClickListener      { filtrar("ervas") }
        chipCosmeticos.setOnClickListener { filtrar("cosmeticos") }

        btnCarrinho.setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
        }

        // Botão Rastreio de Pedidos
        btnPedidos.setOnClickListener {
            startActivity(Intent(this, RastreioPedidosActivity::class.java))
        }

        btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }

        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        carregarProdutos(progressBar)
    }

    private fun carregarProdutos(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        ApiClient.instance.listarProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                progressBar.visibility = View.GONE
                todosProdutos = if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    response.body()!!
                } else {
                    produtosDemo
                }
                adapter.atualizar(todosProdutos)
            }
            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                todosProdutos = produtosDemo
                adapter.atualizar(todosProdutos)
                Toast.makeText(this@HomeActivity, getString(R.string.usando_dados_locais), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrar(categoria: String) {
        val filtrado = if (categoria == "todos") todosProdutos
        else todosProdutos.filter { it.categoria == categoria }
        adapter.atualizar(filtrado)
    }

    override fun onResume() {
        super.onResume()
        val tvCarrinhoQtd = findViewById<TextView>(R.id.tvCarrinhoQtd)
        tvCarrinhoQtd.text = CarrinhoManager.quantidade().toString()
        tvCarrinhoQtd.visibility = if (CarrinhoManager.quantidade() > 0) View.VISIBLE else View.GONE
    }
}
