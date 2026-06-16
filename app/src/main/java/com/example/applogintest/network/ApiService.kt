@file:Suppress("SpellCheckingInspection")
package com.example.applogintest.network

import com.example.applogintest.model.LoginRequest
import com.example.applogintest.model.LoginResponse
import com.example.applogintest.model.CadastroRequest
import com.example.applogintest.model.Pedido
import com.example.applogintest.model.PedidoRequest
import com.example.applogintest.model.PedidoResponse
import com.example.applogintest.model.Produto
import retrofit2.Call
import retrofit2.http.*
import com.example.applogintest.BannerItem
import com.example.applogintest.model.AtualizarEnderecoRequest

interface ApiService {

    // ── Autenticação ──────────────────────────────────────────────────────────
    @POST("/api/clientes/login")
    fun login(@Body body: LoginRequest): Call<LoginResponse>

    @POST("/api/clientes/cadastro")
    fun cadastrar(@Body body: CadastroRequest): Call<Map<String, String>>

    // ── Recuperação de senha ──────────────────────────────────────────────────
    @POST("/api/clientes/esqueci-senha")
    fun solicitarRecuperacao(@Body body: Map<String, String>): Call<Map<String, String>>

    @POST("/api/clientes/redefinir-senha")
    fun redefinirSenha(@Body body: Map<String, String>): Call<Map<String, String>>

    // ── Produtos ──────────────────────────────────────────────────────────────
    @GET("/api/produtos")
    fun listarProdutos(): Call<List<Produto>>

    @GET("/api/produtos")
    fun listarPorCategoria(@Query("categoria_id") categoriaId: Int): Call<List<Produto>>

    @GET("/api/produtos/categorias")
    fun listarCategorias(): Call<List<Map<String, Any>>>

    @GET("/api/banners")
    fun listarBanners(): Call<List<BannerItem>>

    // ── Pedidos ───────────────────────────────────────────────────────────────
    @POST("/api/pedidos")
    fun criarPedido(
        @Header("Authorization") token: String,
        @Body pedido: PedidoRequest
    ): Call<PedidoResponse>

    @GET("/api/pedidos/meus")
    fun listarPedidos(
        @Header("Authorization") token: String
    ): Call<List<Pedido>>

    @PUT("/api/pedidos/{id}/status")
    fun atualizarStatus(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Call<Map<String, String>>

    // Cancelamento pelo cliente — só permitido para pedidos pendentes
    @PUT("/api/pedidos/{id}/cancelar")
    fun cancelarPedido(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<Map<String, String>>

    @PUT("api/clientes/endereco")
    fun atualizarEndereco(
        @Header("Authorization") token: String,
        @Body body: AtualizarEnderecoRequest
    ): Call<Map<String, String>>
}
