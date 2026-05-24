@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.MainActivity
import com.example.applogintest.ProdutoDetalheActivity
import com.example.applogintest.R
import com.example.applogintest.model.Produto
import com.example.applogintest.util.SessionManager

class ProdutoAdapter(
    private var produtos: List<Produto>,
    private val usuarioLogado: Boolean,
    private val onAdicionado: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView           = view.findViewById(R.id.tvProdutoNome)
        val tvDescricao: TextView      = view.findViewById(R.id.tvProdutoDescricao)
        val tvPreco: TextView          = view.findViewById(R.id.tvProdutoPreco)
        val tvCategoria: TextView      = view.findViewById(R.id.tvProdutoCategoria)
        val btnVerDetalhes: Button     = view.findViewById(R.id.btnVerDetalhes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produto = produtos[position]
        holder.tvNome.text      = produto.nome
        holder.tvDescricao.text = produto.descricao
        holder.tvPreco.text     = "R$ %.2f".format(produto.preco)
        holder.tvCategoria.text = produto.categoria.replaceFirstChar { it.uppercase() }

        holder.btnVerDetalhes.setOnClickListener {
            val context = holder.itemView.context
            if (!usuarioLogado) {
                // Não logado: vai para login
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra("from_comprar", true)
                intent.putExtra("produto_nome", produto.nome)
                context.startActivity(intent)
            } else {
                // Logado: abre tela de detalhes
                val intent = Intent(context, ProdutoDetalheActivity::class.java)
                intent.putExtra("produto_id", produto.id)
                intent.putExtra("produto_nome", produto.nome)
                intent.putExtra("produto_descricao", produto.descricao)
                intent.putExtra("produto_preco", produto.preco)
                intent.putExtra("produto_categoria", produto.categoria)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = produtos.size

    fun atualizar(novaLista: List<Produto>) {
        produtos = novaLista
        notifyDataSetChanged()
    }
}
