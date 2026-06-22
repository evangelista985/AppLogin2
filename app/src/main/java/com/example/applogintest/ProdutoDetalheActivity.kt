@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.applogintest.model.CarrinhoManager
import com.example.applogintest.model.Produto
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager

class ProdutoDetalheActivity : AppCompatActivity() {

    private var quantidade = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produto_detalhe)

        val id        = intent.getIntExtra("produto_id", 0)
        val nome      = intent.getStringExtra("produto_nome") ?: ""
        val descricao = intent.getStringExtra("produto_descricao") ?: ""
        val preco     = intent.getDoubleExtra("produto_preco", 0.0)
        val categoria = intent.getStringExtra("produto_categoria") ?: ""
        val imagem    = intent.getStringExtra("produto_imagem") ?: ""

        val produto = Produto(id, nome, descricao, preco, imagem, 0, categoria)

        // Views
        val imgProduto   = findViewById<ImageView>(R.id.imgProdutoDetalhe)
        val tvNome       = findViewById<TextView>(R.id.tvDetalheNome)
        val tvDescricao  = findViewById<TextView>(R.id.tvDetalheDescricao)
        val tvPreco      = findViewById<TextView>(R.id.tvDetalhePreco)
        val tvCategoria  = findViewById<TextView>(R.id.tvDetalheCategoria)
        val tvQuantidade = findViewById<TextView>(R.id.tvQuantidade)
        val btnMenos     = findViewById<Button>(R.id.btnMenos)
        val btnMais      = findViewById<Button>(R.id.btnMais)
        val btnAdicionar = findViewById<Button>(R.id.btnAdicionarCarrinho)
        val btnVoltar    = findViewById<ImageButton>(R.id.btnVoltar)

        // Preenche dados
        tvNome.text       = nome
        tvDescricao.text  = descricao
        tvPreco.text      = "R$ %.2f".format(preco)
        tvCategoria.text  = categoria.replaceFirstChar { it.uppercase() }
        tvQuantidade.text = quantidade.toString()

        // TalkBack: descrição dinâmica da imagem com o nome do produto
        imgProduto.contentDescription = "Foto de $nome"

        // Carrega imagem com Glide
        val urlImagem = when {
            imagem.isBlank()          -> null
            imagem.startsWith("http") -> imagem
            else -> ApiClient.BASE_URL.trimEnd('/') + imagem
        }

        if (urlImagem != null) {
            Glide.with(this)
                .load(urlImagem)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(imgProduto)
        } else {
            imgProduto.setImageResource(R.mipmap.ic_launcher)
        }

        btnVoltar.setOnClickListener { finish() }

        btnMenos.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                tvQuantidade.text = quantidade.toString()
                // TalkBack: anuncia a nova quantidade automaticamente (accessibilityLiveRegion)
                tvQuantidade.contentDescription = "Quantidade: $quantidade"
            }
        }

        btnMais.setOnClickListener {
            quantidade++
            tvQuantidade.text = quantidade.toString()
            // TalkBack: anuncia a nova quantidade automaticamente (accessibilityLiveRegion)
            tvQuantidade.contentDescription = "Quantidade: $quantidade"
        }

        btnAdicionar.setOnClickListener {
            if (!SessionManager.estaLogado(this)) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("from_comprar", true)
                intent.putExtra("produto_nome", nome)
                startActivity(intent)
            } else {
                repeat(quantidade) { CarrinhoManager.adicionar(produto) }
                Toast.makeText(this, "$quantidade x $nome adicionado ao carrinho!", Toast.LENGTH_SHORT).show()
                // TalkBack: anuncia confirmação da ação ao usuário
                btnAdicionar.announceForAccessibility("$quantidade x $nome adicionado ao carrinho")
                finish()
            }
        }
    }
}
