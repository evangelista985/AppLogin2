package com.example.applogintest.util

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME   = "pura_essencia_prefs"
    private const val KEY_TOKEN   = "token"
    private const val KEY_NOME    = "nome"
    private const val KEY_EMAIL   = "email"
    private const val KEY_CEP     = "cep"
    private const val KEY_ENDERECO = "endereco"
    private const val KEY_NUMERO  = "numero"
    private const val KEY_BAIRRO  = "bairro"
    private const val KEY_CIDADE  = "cidade"
    private const val KEY_ESTADO  = "estado"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun salvar(
        context: Context,
        token: String,
        nome: String,
        email: String,
        cep: String = "",
        endereco: String = "",
        numero: String = "",
        bairro: String = "",
        cidade: String = "",
        estado: String = ""
    ) {
        prefs(context).edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_NOME, nome)
            .putString(KEY_EMAIL, email)
            .putString(KEY_CEP, cep)
            .putString(KEY_ENDERECO, endereco)
            .putString(KEY_NUMERO, numero)
            .putString(KEY_BAIRRO, bairro)
            .putString(KEY_CIDADE, cidade)
            .putString(KEY_ESTADO, estado)
            .apply()
    }

    fun getToken(context: Context): String?   = prefs(context).getString(KEY_TOKEN, null)
    fun getNome(context: Context): String     = prefs(context).getString(KEY_NOME, "Cliente") ?: "Cliente"
    fun getEmail(context: Context): String    = prefs(context).getString(KEY_EMAIL, "") ?: ""
    fun getCep(context: Context): String      = prefs(context).getString(KEY_CEP, "") ?: ""
    fun getEndereco(context: Context): String = prefs(context).getString(KEY_ENDERECO, "") ?: ""
    fun getNumero(context: Context): String   = prefs(context).getString(KEY_NUMERO, "") ?: ""
    fun getBairro(context: Context): String   = prefs(context).getString(KEY_BAIRRO, "") ?: ""
    fun getCidade(context: Context): String   = prefs(context).getString(KEY_CIDADE, "") ?: ""
    fun getEstado(context: Context): String   = prefs(context).getString(KEY_ESTADO, "") ?: ""

    fun temEnderecoCompleto(context: Context): Boolean =
        getCep(context).isNotEmpty() && getEndereco(context).isNotEmpty()

    fun estaLogado(context: Context): Boolean = getToken(context) != null

    fun logout(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
