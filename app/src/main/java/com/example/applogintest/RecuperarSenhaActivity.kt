@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarSenhaActivity : AppCompatActivity() {

    private var emailDigitado = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_senha)

        val layoutEtapa1    = findViewById<View>(R.id.layoutEtapa1)
        val layoutEtapa2    = findViewById<View>(R.id.layoutEtapa2)
        val etEmail         = findViewById<EditText>(R.id.etEmail)
        val etCodigo        = findViewById<EditText>(R.id.etCodigo)
        val etNovaSenha     = findViewById<EditText>(R.id.etNovaSenha)
        val etConfirmar     = findViewById<EditText>(R.id.etConfirmarSenha)
        val btnEnviar       = findViewById<Button>(R.id.btnEnviarCodigo)
        val btnRedefinir    = findViewById<Button>(R.id.btnRedefinir)
        val btnReenviar     = findViewById<Button>(R.id.btnReenviar)
        val btnVoltar       = findViewById<Button>(R.id.btnVoltar)
        val tvEmailEnviado  = findViewById<TextView>(R.id.tvEmailEnviado)
        val progressBar     = findViewById<ProgressBar>(R.id.progressBar)

        btnVoltar.setOnClickListener { finish() }

        // ETAPA 1: Enviar código
        btnEnviar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Digite um email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            emailDigitado = email
            progressBar.visibility = View.VISIBLE
            btnEnviar.isEnabled = false

            ApiClient.instance.solicitarRecuperacao(mapOf("email" to email))
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        progressBar.visibility = View.GONE
                        btnEnviar.isEnabled = true
                        if (response.isSuccessful) {
                            tvEmailEnviado.text = "Código enviado para: $email"
                            layoutEtapa1.visibility = View.GONE
                            layoutEtapa2.visibility = View.VISIBLE
                            Toast.makeText(this@RecuperarSenhaActivity, "Código enviado! Verifique seu email.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@RecuperarSenhaActivity, "Email não encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnEnviar.isEnabled = true
                        Toast.makeText(this@RecuperarSenhaActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ETAPA 2: Redefinir senha
        btnRedefinir.setOnClickListener {
            val codigo     = etCodigo.text.toString().trim()
            val novaSenha  = etNovaSenha.text.toString().trim()
            val confirmar  = etConfirmar.text.toString().trim()

            if (codigo.length != 6) {
                Toast.makeText(this, "Digite o código de 6 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (novaSenha.length < 6) {
                Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (novaSenha != confirmar) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnRedefinir.isEnabled = false

            val body = mapOf(
                "email"     to emailDigitado,
                "codigo"    to codigo,
                "novaSenha" to novaSenha
            )

            ApiClient.instance.redefinirSenha(body)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        progressBar.visibility = View.GONE
                        btnRedefinir.isEnabled = true
                        if (response.isSuccessful) {
                            Toast.makeText(this@RecuperarSenhaActivity, "Senha redefinida com sucesso! Faça login.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            Toast.makeText(this@RecuperarSenhaActivity, "Código inválido ou expirado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnRedefinir.isEnabled = true
                        Toast.makeText(this@RecuperarSenhaActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Reenviar código
        btnReenviar.setOnClickListener {
            layoutEtapa1.visibility = View.VISIBLE
            layoutEtapa2.visibility = View.GONE
            etCodigo.text.clear()
            etNovaSenha.text.clear()
            etConfirmar.text.clear()
        }
    }
}
