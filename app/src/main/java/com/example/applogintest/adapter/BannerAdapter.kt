@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.applogintest.BannerItem
import com.example.applogintest.R
import com.example.applogintest.network.ApiClient

class BannerAdapter(
    private val banners: MutableList<BannerItem>
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rootView    = view
        val imgBanner   = view.findViewById<ImageView>(R.id.imgBanner)
        val tvTitulo    = view.findViewById<TextView>(R.id.tvBannerTitulo)
        val tvSubtitulo = view.findViewById<TextView>(R.id.tvBannerSubtitulo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val banner = banners[position]
        holder.tvTitulo.text    = banner.titulo ?: ""
        holder.tvSubtitulo.text = banner.subtitulo ?: ""

        val urlImagem = when {
            banner.imagem.isNullOrBlank()    -> null
            banner.imagem.startsWith("http") -> banner.imagem
            else -> ApiClient.BASE_URL.trimEnd('/') + banner.imagem
        }

        // Aplica cor de fundo no rootView
        try {
            val cor = if (banner.cor_fundo.isNullOrBlank()) "#1B4D1A" else banner.cor_fundo
            holder.rootView.setBackgroundColor(Color.parseColor(cor))
        } catch (e: Exception) {
            holder.rootView.setBackgroundColor(Color.parseColor("#1B4D1A"))
        }

        if (urlImagem != null) {
            holder.imgBanner.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(urlImagem)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.imgBanner)
        } else {
            holder.imgBanner.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount() = banners.size

    fun atualizarBanners(novaLista: List<BannerItem>) {
        banners.clear()
        banners.addAll(novaLista)
        notifyDataSetChanged()
    }
}