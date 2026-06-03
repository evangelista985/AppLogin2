@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applogintest.model.LoginRequest
import com.example.applogintest.model.LoginResponse
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fromComprar = intent.getBooleanExtra("from_comprar", false)
        if (!fromComprar && SessionManager.estaLogado(this)) {
            irParaHome()
            return
        }

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val etEmail      = findViewById<EditText>(R.id.editTextEmail)
        val etSenha      = findViewById<EditText>(R.id.editTextSenha)
        val btnLogin     = findViewById<Button>(R.id.button)
        val btnCadastrar = findViewById<Button>(R.id.btnCadastrar)
        val tvEsqueci    = findViewById<TextView>(R.id.tvEsqueciSenha)
        val progressBar  = findViewById<ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false

            ApiClient.instance.login(LoginRequest(email, senha))
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        if (response.isSuccessful) {
                            val body = response.body()!!
                            SessionManager.salvar(
                                context  = this@MainActivity,
                                token    = body.token,
                                id       = body.cliente.id,
                                nome     = body.cliente.nome,
                                email    = body.cliente.email,
                                cep      = body.cliente.cep      ?: "",
                                endereco = body.cliente.endereco ?: "",
                                numero   = body.cliente.numero   ?: "",
                                bairro   = body.cliente.bairro   ?: "",
                                cidade   = body.cliente.cidade   ?: "",
                                estado   = body.cliente.estado   ?: ""
                            )
                            irParaHome()
                        } else {
                            Toast.makeText(this@MainActivity, "E-mail ou senha inválidos", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnLogin.isEnabled = true
                        Toast.makeText(this@MainActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        btnCadastrar.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        tvEsqueci.setOnClickListener {
            startActivity(Intent(this, RecuperarSenhaActivity::class.java))
        }
    }

    private fun irParaHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
