@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.R
import com.example.applogintest.model.Pedido
import java.text.SimpleDateFormat
import java.util.*

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

    private fun formatarData(dataIso: String?): String {
        if (dataIso.isNullOrEmpty()) return ""
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(dataIso) ?: return dataIso
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            formatter.timeZone = TimeZone.getDefault()
            formatter.format(date)
        } catch (e: Exception) {
            dataIso
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pedido  = pedidos[position]
        val verde   = 0xFF023804.toInt()
        val cinza   = 0xFFD0D0D0.toInt()

        holder.tvId.text        = "Pedido #${pedido.id}"
        holder.tvData.text      = formatarData(pedido.criado_em)
        val valorExibir         = pedido.total_final ?: pedido.total ?: 0.0
        holder.tvTotal.text     = "Total: R$ %.2f".format(valorExibir)
        holder.tvPagamento.text = pedido.forma_pagamento ?: ""

        // Verifica se o status é válido para mostrar rastreio
        val statusValido = listOf("pago", "pagos", "enviado", "entregue",
            "preparacao", "preparação", "transporte")
        val status = pedido.status?.lowercase() ?: ""

        if (status.isEmpty() || status == "pendente" || status == "cancelado") {
            holder.layoutRastreio.visibility = View.GONE
            holder.tvStatusAberto.visibility = View.VISIBLE
            return
        }

        holder.layoutRastreio.visibility = View.VISIBLE
        holder.tvStatusAberto.visibility = View.GONE

        // Converte status do Node para etapa numérica
        val etapa = when (status) {
            "pago", "pagos", "preparacao", "preparação" -> 0
            "enviado", "transporte"                      -> 1
            "entregue", "finalizado"                     -> 2
            else                                         -> 0
        }

        // Aplica cores SEM animação — verde=concluído/atual, cinza=futuro
        holder.icPreparacao.setBackgroundColor(verde)
        holder.linhaEtapa1.setBackgroundColor(if (etapa >= 1) verde else cinza)
        holder.icTransportadora.setBackgroundColor(if (etapa >= 1) verde else cinza)
        holder.linhaEtapa2.setBackgroundColor(if (etapa >= 2) verde else cinza)
        holder.icEntregue.setBackgroundColor(if (etapa >= 2) verde else cinza)

        // Sem animações — ícones estáticos
        holder.icPreparacao.clearAnimation()
        holder.icTransportadora.clearAnimation()
        holder.icEntregue.clearAnimation()
    }

    override fun getItemCount() = pedidos.size

    fun atualizar(novaLista: List<Pedido>) {
        pedidos = novaLista
        notifyDataSetChanged()
    }

    // Mantido para compatibilidade com RastreioPedidosActivity
    fun limparHandlers() {}
}
