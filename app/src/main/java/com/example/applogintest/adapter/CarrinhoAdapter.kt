package com.example.applogintest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.R
import com.example.applogintest.model.CarrinhoItem

class CarrinhoAdapter(
    private var itens: List<CarrinhoItem>,
    private val onRemover: (Int) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNome: TextView      = view.findViewById(R.id.tvCarrinhoNome)
        val tvQtd: TextView       = view.findViewById(R.id.tvCarrinhoQtd)
        val tvSubtotal: TextView  = view.findViewById(R.id.tvCarrinhoSubtotal)
        val btnRemover: Button    = view.findViewById(R.id.btnRemoverCarrinho)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_carrinho, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itens[position]
        holder.tvNome.text     = item.produto.nome
        holder.tvQtd.text      = "Qtd: ${item.quantidade}"
        holder.tvSubtotal.text = "R$ %.2f".format(item.subtotal)
        holder.btnRemover.setOnClickListener { onRemover(item.produto.id) }
    }

    override fun getItemCount() = itens.size

    fun atualizar(novaLista: List<CarrinhoItem>) {
        itens = novaLista
        notifyDataSetChanged()
    }
}
