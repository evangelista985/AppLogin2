@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applogintest.model.CadastroRequest
import com.example.applogintest.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val etNome      = findViewById<EditText>(R.id.editTextNome)
        val etEmail     = findViewById<EditText>(R.id.editTextEmail)
        val etTelefone  = findViewById<EditText>(R.id.editTextTelefone)
        val etSenha     = findViewById<EditText>(R.id.editTextSenha)
        val btnCadastro = findViewById<Button>(R.id.btnCadastro)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnCadastro.setOnClickListener {
            val nome  = etNome.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val senha = etSenha.text.toString().trim()

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Nome, e-mail e senha são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (senha.length < 6) {
                Toast.makeText(this, "Senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnCadastro.isEnabled  = false

            val request = CadastroRequest(
                nome     = nome,
                email    = email,
                senha    = senha,
                telefone = etTelefone.text.toString().trim().ifEmpty { null }
            )

            ApiClient.instance.cadastrar(request)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                        progressBar.visibility = View.GONE
                        btnCadastro.isEnabled  = true
                        if (response.isSuccessful) {
                            Toast.makeText(this@CadastroActivity, "Cadastro realizado! Faça login.", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            val erro = response.errorBody()?.string() ?: "Erro ao cadastrar"
                            Toast.makeText(this@CadastroActivity, erro, Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                        progressBar.visibility = View.GONE
                        btnCadastro.isEnabled  = true
                        Toast.makeText(this@CadastroActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
