@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.applogintest.R
import com.example.applogintest.model.Pedido
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class PedidoAdapter(
    private var pedidos: List<Pedido>,
    private val onCancelado: () -> Unit = {}
) : RecyclerView.Adapter<PedidoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId               = view.findViewById<TextView>(R.id.tvPedidoId)
        val tvData             = view.findViewById<TextView>(R.id.tvPedidoData)
        val tvTotal            = view.findViewById<TextView>(R.id.tvPedidoTotal)
        val tvPagamento        = view.findViewById<TextView>(R.id.tvPedidoPagamento)
        val layoutRastreio     = view.findViewById<View>(R.id.layoutRastreio)
        val layoutStatusAberto = view.findViewById<View>(R.id.layoutStatusAberto)
        val tvStatusIcone      = view.findViewById<TextView>(R.id.tvStatusIcone)
        val tvStatusAberto     = view.findViewById<TextView>(R.id.tvStatusAberto)
        val icPreparacao       = view.findViewById<TextView>(R.id.icPreparacao)
        val icTransportadora   = view.findViewById<TextView>(R.id.icTransportadora)
        val icEntregue         = view.findViewById<TextView>(R.id.icEntregue)
        val linhaEtapa1        = view.findViewById<View>(R.id.linhaEtapa1)
        val linhaEtapa2        = view.findViewById<View>(R.id.linhaEtapa2)
        val btnCancelar        = view.findViewById<Button>(R.id.btnCancelarPedido)
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
        val pedido = pedidos[position]
        val verde  = 0xFF023804.toInt()
        val cinza  = 0xFFD0D0D0.toInt()

        holder.tvId.text        = "Pedido #${pedido.id}"
        holder.tvData.text      = formatarData(pedido.criado_em)
        val valorExibir         = pedido.total_final ?: pedido.total ?: 0.0
        holder.tvTotal.text     = "Total: R$ %.2f".format(valorExibir)
        holder.tvPagamento.text = pedido.forma_pagamento ?: ""

        val status = pedido.status?.lowercase() ?: ""

        // Oculta botão cancelar por padrão — só mostra para pendente
        holder.btnCancelar.visibility = View.GONE

        // Pedido pendente (aguardando confirmação de pagamento via Pix/Boleto)
        if (status == "pendente") {
            holder.layoutRastreio.visibility = View.GONE
            holder.layoutStatusAberto.visibility = View.VISIBLE
            holder.tvStatusIcone.text = "⏳"
            holder.tvStatusAberto.text = "Pagamento em processamento"

            // Mostra botão cancelar para pedidos pendentes
            holder.btnCancelar.visibility = View.VISIBLE
            holder.btnCancelar.setOnClickListener {
                val context = holder.itemView.context
                AlertDialog.Builder(context)
                    .setTitle("Cancelar pedido #${pedido.id}?")
                    .setMessage("Esta ação não pode ser desfeita. O estoque será restaurado automaticamente.")
                    .setPositiveButton("Sim, cancelar") { _, _ ->
                        cancelarPedido(holder, pedido.id ?: return@setPositiveButton)
                    }
                    .setNegativeButton("Voltar", null)
                    .show()
            }
            return
        }

        // Pedido cancelado
        if (status == "cancelado") {
            holder.layoutRastreio.visibility = View.GONE
            holder.layoutStatusAberto.visibility = View.VISIBLE
            holder.tvStatusIcone.text = "❌"
            holder.tvStatusAberto.text = "Pedido cancelado"
            return
        }

        // Status desconhecido/vazio: não mostra nada extra
        if (status.isEmpty()) {
            holder.layoutRastreio.visibility = View.GONE
            holder.layoutStatusAberto.visibility = View.GONE
            return
        }

        holder.layoutRastreio.visibility = View.VISIBLE
        holder.layoutStatusAberto.visibility = View.GONE

        // Converte status do Node para etapa numérica
        val etapa = when (status) {
            "pago", "pagos", "preparacao", "preparação" -> 0
            "enviado", "transporte"                      -> 1
            "entregue", "finalizado"                     -> 2
            else                                         -> 0
        }

        // Aplica cores — verde=concluído/atual, cinza=futuro
        holder.icPreparacao.setBackgroundColor(verde)
        holder.linhaEtapa1.setBackgroundColor(if (etapa >= 1) verde else cinza)
        holder.icTransportadora.setBackgroundColor(if (etapa >= 1) verde else cinza)
        holder.linhaEtapa2.setBackgroundColor(if (etapa >= 2) verde else cinza)
        holder.icEntregue.setBackgroundColor(if (etapa >= 2) verde else cinza)

        holder.icPreparacao.clearAnimation()
        holder.icTransportadora.clearAnimation()
        holder.icEntregue.clearAnimation()
    }

    private fun cancelarPedido(holder: ViewHolder, pedidoId: Long) {
        val context = holder.itemView.context
        val token = SessionManager.getBearerToken(context)

        holder.btnCancelar.isEnabled = false
        holder.btnCancelar.text = "Cancelando..."

        ApiClient.instance.cancelarPedido(token, pedidoId)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Pedido #$pedidoId cancelado com sucesso.", Toast.LENGTH_LONG).show()
                        onCancelado() // recarrega a lista na Activity
                    } else {
                        // Exibe a mensagem de erro do backend (ex: "Este pedido já está pago...")
                        val erro = try {
                            val json = response.errorBody()?.string() ?: ""
                            // extrai o campo "erro" do JSON manualmente
                            val match = Regex("\"erro\"\\s*:\\s*\"([^\"]+)\"").find(json)
                            match?.groupValues?.get(1) ?: "Não foi possível cancelar o pedido."
                        } catch (e: Exception) {
                            "Não foi possível cancelar o pedido."
                        }
                        AlertDialog.Builder(context)
                            .setTitle("Não foi possível cancelar")
                            .setMessage(erro)
                            .setPositiveButton("OK", null)
                            .show()
                        holder.btnCancelar.isEnabled = true
                        holder.btnCancelar.text = "Cancelar pedido"
                    }
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(context, "Falha de conexão. Tente novamente.", Toast.LENGTH_LONG).show()
                    holder.btnCancelar.isEnabled = true
                    holder.btnCancelar.text = "Cancelar pedido"
                }
            })
    }

    override fun getItemCount() = pedidos.size

    fun atualizar(novaLista: List<Pedido>) {
        pedidos = novaLista
        notifyDataSetChanged()
    }

    fun limparHandlers() {}
}
