@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.R
import com.example.applogintest.model.Pedido

class PedidoAdapter(
    private var pedidos: List<Pedido>
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId             = view.findViewById<TextView>(R.id.tvPedidoId)
        val tvData           = view.findViewById<TextView>(R.id.tvPedidoData)
        val tvTotal          = view.findViewById<TextView>(R.id.tvPedidoTotal)
        val tvPagamento      = view.findViewById<TextView>(R.id.tvPedidoPagamento)
        val layoutRastreio   = view.findViewById<View>(R.id.layoutRastreio)
        val tvStatusAberto   = view.findViewById<TextView>(R.id.tvStatusAberto)
        val icPreparacao     = view.findViewById<TextView>(R.id.icPreparacao)
        val icTransportadora = view.findViewById<TextView>(R.id.icTransportadora)
        val icEntregue       = view.findViewById<TextView>(R.id.icEntregue)
        val linhaEtapa1      = view.findViewById<View>(R.id.linhaEtapa1)
        val linhaEtapa2      = view.findViewById<View>(R.id.linhaEtapa2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pedido, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido  = pedidos[position]
        val verde   = 0xFF023804.toInt()
        val cinza   = 0xFFD0D0D0.toInt()
        val context = holder.itemView.context

        holder.tvId.text        = "Pedido #${pedido.id}"
        holder.tvData.text      = pedido.dataPedido ?: ""
        holder.tvTotal.text     = "Total: R$ %.2f".format(pedido.total ?: 0.0)
        holder.tvPagamento.text = pedido.formaPagamento ?: ""

        // Para animações anteriores
        holder.icPreparacao.clearAnimation()
        holder.icTransportadora.clearAnimation()
        holder.icEntregue.clearAnimation()

        if (pedido.status == "pagos") {
            holder.layoutRastreio.visibility = View.VISIBLE
            holder.tvStatusAberto.visibility = View.GONE

            val etapa = pedido.statusRastreio ?: 0

            // Cores dos ícones e linhas
            holder.icPreparacao.setBackgroundColor(if (etapa >= 0) verde else cinza)
            holder.icTransportadora.setBackgroundColor(if (etapa >= 1) verde else cinza)
            holder.linhaEtapa1.setBackgroundColor(if (etapa >= 1) verde else cinza)
            holder.icEntregue.setBackgroundColor(if (etapa >= 2) verde else cinza)
            holder.linhaEtapa2.setBackgroundColor(if (etapa >= 2) verde else cinza)

            // Animações por etapa atual
            when (etapa) {
                0 -> {
                    // Preparação pulsando
                    val animPulsar = AnimationUtils.loadAnimation(context, R.anim.anim_pulsar)
                    holder.icPreparacao.startAnimation(animPulsar)
                }
                1 -> {
                    // Transportadora deslizando
                    val animDeslizar = AnimationUtils.loadAnimation(context, R.anim.anim_deslizar)
                    holder.icTransportadora.startAnimation(animDeslizar)
                }
                2 -> {
                    // Entregue com fade/escala
                    val animConfirmar = AnimationUtils.loadAnimation(context, R.anim.anim_confirmar)
                    holder.icEntregue.startAnimation(animConfirmar)
                }
            }

        } else {
            holder.layoutRastreio.visibility = View.GONE
            holder.tvStatusAberto.visibility = View.VISIBLE
        }
    }

    override fun getItemCount() = pedidos.size

    fun atualizar(novaLista: List<Pedido>) {
        pedidos = novaLista
        notifyDataSetChanged()
    }
}
