@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.CarrinhoManager
import com.example.applogintest.model.Produto
import com.example.applogintest.util.SessionManager

class ProdutoDetalheActivity : AppCompatActivity() {

    private var quantidade = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produto_detalhe)

        // Recebe o produto via Intent
        val id        = intent.getIntExtra("produto_id", 0)
        val nome      = intent.getStringExtra("produto_nome") ?: ""
        val descricao = intent.getStringExtra("produto_descricao") ?: ""
        val preco     = intent.getDoubleExtra("produto_preco", 0.0)
        val categoria = intent.getStringExtra("produto_categoria") ?: ""
        val produto   = Produto(id, nome, descricao, preco, categoria)

        // Views
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
        tvNome.text      = nome
        tvDescricao.text = descricao
        tvPreco.text     = "R$ %.2f".format(preco)
        tvCategoria.text = categoria.replaceFirstChar { it.uppercase() }
        tvQuantidade.text = quantidade.toString()

        // Botão voltar
        btnVoltar.setOnClickListener { finish() }

        // Seletor de quantidade
        btnMenos.setOnClickListener {
            if (quantidade > 1) {
                quantidade--
                tvQuantidade.text = quantidade.toString()
            }
        }

        btnMais.setOnClickListener {
            quantidade++
            tvQuantidade.text = quantidade.toString()
        }

        // Botão Adicionar ao Carrinho
        btnAdicionar.setOnClickListener {
            if (!SessionManager.estaLogado(this)) {
                // Não logado: vai para login
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("from_comprar", true)
                intent.putExtra("produto_nome", nome)
                startActivity(intent)
            } else {
                // Logado: adiciona a quantidade selecionada ao carrinho
                repeat(quantidade) {
                    CarrinhoManager.adicionar(produto)
                }
                Toast.makeText(
                    this,
                    "$quantidade x $nome adicionado ao carrinho!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
