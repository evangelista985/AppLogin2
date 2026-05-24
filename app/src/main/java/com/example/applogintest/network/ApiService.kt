package com.example.applogintest.network

import com.example.applogintest.model.Pedido
import com.example.applogintest.model.Produto
import com.example.applogintest.model.Usuario
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ── Autenticação ──────────────────────────────────────────────────────────
    @POST("/login")
    fun login(@Body usuario: Usuario): Call<Usuario>

    @POST("/cadastrar")
    fun cadastrar(@Body usuario: Usuario): Call<Usuario>

    // ── Recuperação de senha ──────────────────────────────────────────────────
    @POST("/recuperar-senha/solicitar")
    fun solicitarRecuperacao(@Body body: Map<String, String>): Call<Map<String, String>>

    @POST("/recuperar-senha/redefinir")
    fun redefinirSenha(@Body body: Map<String, String>): Call<Map<String, String>>

    // ── Email confirmação pedido ──────────────────────────────────────────────
    @POST("/pedidos/confirmar-email")
    fun confirmarEmailPedido(@Body body: Map<String, String>): Call<Map<String, String>>

    // ── Usuários ──────────────────────────────────────────────────────────────
    @GET("/usuarios")
    fun listarUsuarios(): Call<List<Usuario>>

    // ── Produtos ──────────────────────────────────────────────────────────────
    @GET("/api/produtos")
    fun listarProdutos(): Call<List<Produto>>

    @GET("/api/produtos/categoria/{categoria}")
    fun listarPorCategoria(@Path("categoria") categoria: String): Call<List<Produto>>

    // ── Pedidos ───────────────────────────────────────────────────────────────
    @POST("/pedidos")
    fun criarPedido(@Body pedido: Pedido): Call<Pedido>

    @GET("/pedidos/usuario/{usuarioId}")
    fun listarPedidos(@Path("usuarioId") usuarioId: Long): Call<List<Pedido>>

    @GET("/pedidos/usuario/{usuarioId}/status/{status}")
    fun listarPedidosPorStatus(
        @Path("usuarioId") usuarioId: Long,
        @Path("status") status: String
    ): Call<List<Pedido>>

    @PUT("/pedidos/{id}/rastreio/{statusRastreio}")
    fun atualizarRastreio(
        @Path("id") id: Long,
        @Path("statusRastreio") statusRastreio: Int
    ): Call<Pedido>
}
