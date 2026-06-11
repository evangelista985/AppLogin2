@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.applogintest.adapter.BannerAdapter
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
    private lateinit var bannerAdapter: BannerAdapter
    private var todosProdutos = listOf<Produto>()
    private val bannerHandler = Handler(Looper.getMainLooper())
    private var bannerRunnable: Runnable? = null

    private val produtosDemo = listOf(
        Produto(1, "Chá Camomila", "Relaxante e calmante", 12.90, "", 0, "Chás"),
        Produto(2, "Alecrim", "Tempero aromático", 8.50, "", 0, "Temperos")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica modo escuro salvo
        val prefs = getSharedPreferences("pura_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("modo_escuro", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val logado = SessionManager.estaLogado(this)
        val tvSaudacao = findViewById<TextView>(R.id.tvSaudacao)

        if (logado) {
            tvSaudacao.text = "Olá, ${SessionManager.getNome(this)}"
            tvSaudacao.visibility = View.VISIBLE
        } else {
            tvSaudacao.visibility = View.GONE
        }

        findViewById<ImageButton>(R.id.btnCarrinho).setOnClickListener {
            startActivity(Intent(this, CarrinhoActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnPedidos).setOnClickListener {
            startActivity(Intent(this, RastreioPedidosActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnAdmin).setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnLogout).setOnClickListener {
            SessionManager.logout(this)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        configurarCarrossel()
        configurarBusca()
        configurarFiltros()
        configurarBottomNav(logado)
        configurarRecycler(logado)
        carregarProdutos()
        carregarBanners()
        atualizarBadgeCarrinho()
    }

    private fun configurarCarrossel() {
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerBanner)
        val layoutIndicadores = findViewById<LinearLayout>(R.id.layoutIndicadores)

        val bannersDefault = listOf(
            BannerItem(titulo = "Bem-vindo", subtitulo = "Natureza e saúde para você", cor_fundo = "#1B4D1A"),
            BannerItem(titulo = "Cosméticos", subtitulo = "Cuide da sua pele", cor_fundo = "#4A2D6B"),
            BannerItem(titulo = "Temperos", subtitulo = "Sabor e saúde", cor_fundo = "#7A2A0A")
        )

        bannerAdapter = BannerAdapter(bannersDefault.toMutableList())
        viewPager.adapter = bannerAdapter

        fun atualizarIndicadores(total: Int) {
            layoutIndicadores.removeAllViews()
            val dots = Array(total) { View(this) }
            dots.forEachIndexed { i, dot ->
                val p = LinearLayout.LayoutParams(if (i == 0) 20 else 8, 8)
                p.setMargins(4, 0, 4, 0)
                dot.layoutParams = p
                dot.setBackgroundResource(
                    if (i == 0) android.R.drawable.presence_online
                    else android.R.drawable.presence_invisible
                )
                layoutIndicadores.addView(dot)
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    dots.forEachIndexed { i, dot ->
                        val lp = dot.layoutParams as LinearLayout.LayoutParams
                        lp.width = if (i == position) 20 else 8
                        dot.layoutParams = lp
                        dot.alpha = if (i == position) 1f else 0.4f
                    }
                }
            })
        }

        atualizarIndicadores(bannersDefault.size)

        bannerRunnable = object : Runnable {
            override fun run() {
                val next = (viewPager.currentItem + 1) % (bannerAdapter.itemCount)
                viewPager.setCurrentItem(next, true)
                bannerHandler.postDelayed(this, 4000)
            }
        }
        bannerHandler.postDelayed(bannerRunnable!!, 4000)
    }

    private fun carregarBanners() {
        ApiClient.instance.listarBanners().enqueue(object : Callback<List<BannerItem>> {
            override fun onResponse(call: Call<List<BannerItem>>, response: Response<List<BannerItem>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    bannerAdapter.atualizarBanners(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<BannerItem>>, t: Throwable) {}
        })
    }

    private fun configurarBusca() {
        val etBusca = findViewById<EditText>(R.id.etBusca)
        val ivLimparBusca = findViewById<ImageView>(R.id.ivLimparBusca)

        etBusca.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                ivLimparBusca.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                val filtrados = if (query.isEmpty()) todosProdutos
                else todosProdutos.filter {
                    it.nome.lowercase().contains(query) ||
                            it.descricao.lowercase().contains(query) ||
                            (it.categoria_nome?.lowercase()?.contains(query) ?: false)
                }
                adapter.atualizar(filtrados)
            }
        })

        ivLimparBusca.setOnClickListener {
            etBusca.setText("")
            etBusca.clearFocus()
            ivLimparBusca.visibility = View.GONE
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etBusca.windowToken, 0)
            adapter.atualizar(todosProdutos)
        }

        etBusca.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etBusca.windowToken, 0)
                true
            } else false
        }
    }

    private fun configurarFiltros() {
        data class Chip(val id: Int, val filtro: String)
        val chips = listOf(
            Chip(R.id.chipTodos,      "todos"),
            Chip(R.id.chipChas,       "chas"),
            Chip(R.id.chipTemperos,   "temperos"),
            Chip(R.id.chipErvas,      "organicos"),
            Chip(R.id.chipCosmeticos, "cosmeticos")
        )
        chips.forEach { chip ->
            findViewById<TextView>(chip.id).setOnClickListener {
                chips.forEach { c ->
                    val btn = findViewById<TextView>(c.id)
                    if (c.id == chip.id) {
                        btn.backgroundTintList = ColorStateList.valueOf(0xFF023804.toInt())
                        btn.setTextColor(0xFFFFFDD0.toInt())
                    } else {
                        btn.backgroundTintList = ColorStateList.valueOf(0xFF5D8A3E.toInt())
                        btn.setTextColor(0xFFFFFFFF.toInt())
                    }
                }
                val filtrados = when (chip.filtro) {
                    "todos"      -> todosProdutos
                    "chas"       -> todosProdutos.filter { it.categoria_nome?.lowercase()?.contains("ch") ?: false }
                    "temperos"   -> todosProdutos.filter { it.categoria_nome?.lowercase()?.contains("tempero") ?: false }
                    "organicos"  -> todosProdutos.filter { it.categoria_nome?.lowercase()?.contains("org") ?: false }
                    "cosmeticos" -> todosProdutos.filter { it.categoria_nome?.lowercase()?.contains("cosm") ?: false }
                    else         -> todosProdutos
                }
                adapter.atualizar(filtrados)
            }
        }
    }

    private fun configurarBottomNav(logado: Boolean) {
        findViewById<LinearLayout>(R.id.navInicio).setOnClickListener { }
        findViewById<LinearLayout>(R.id.navCarrinho).setOnClickListener {
            if (logado) startActivity(Intent(this, CarrinhoActivity::class.java))
            else startActivity(Intent(this, MainActivity::class.java).putExtra("from_comprar", true))
        }
        findViewById<LinearLayout>(R.id.navPedidos).setOnClickListener {
            if (logado) startActivity(Intent(this, RastreioPedidosActivity::class.java))
            else startActivity(Intent(this, MainActivity::class.java).putExtra("from_comprar", true))
        }
        findViewById<LinearLayout>(R.id.navAdmin).setOnClickListener {
            if (logado) startActivity(Intent(this, AdminActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.navSair).setOnClickListener {
            if (logado) {
                SessionManager.logout(this)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }
    }

    private fun configurarRecycler(logado: Boolean) {
        adapter = ProdutoAdapter(emptyList(), logado) { produto ->
            Toast.makeText(this, "${produto.nome} adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
            atualizarBadgeCarrinho()
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerProdutos)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun carregarProdutos() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        ApiClient.instance.listarProdutos().enqueue(object : Callback<List<Produto>> {
            override fun onResponse(call: Call<List<Produto>>, response: Response<List<Produto>>) {
                progressBar.visibility = View.GONE
                todosProdutos = if (response.isSuccessful && !response.body().isNullOrEmpty())
                    response.body()!! else produtosDemo
                adapter.atualizar(todosProdutos)
            }
            override fun onFailure(call: Call<List<Produto>>, t: Throwable) {
                progressBar.visibility = View.GONE
                todosProdutos = produtosDemo
                adapter.atualizar(todosProdutos)
                Toast.makeText(this@HomeActivity, "Erro de conexão", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun atualizarBadgeCarrinho() {
        val qtd = CarrinhoManager.quantidade()
        val navBadge = findViewById<TextView>(R.id.tvNavCarrinhoQtd)
        navBadge.text = qtd.toString()
        navBadge.visibility = if (qtd > 0) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        atualizarBadgeCarrinho()
    }

    override fun onDestroy() {
        super.onDestroy()
        bannerRunnable?.let { bannerHandler.removeCallbacks(it) }
    }
}