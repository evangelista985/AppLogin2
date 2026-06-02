@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ViaCepResp(
    val logradouro: String?,
    val bairro: String?,
    val localidade: String?,
    val uf: String?,
    val erro: Boolean? = false
)

interface ViaCepApi {
    @GET("{cep}/json/")
    fun buscar(@Path("cep") cep: String): Call<ViaCepResp>
}

class CadastroActivity : AppCompatActivity() {

    private val viaCep: ViaCepApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ViaCepApi::class.java)
    }

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
        val etCep       = findViewById<EditText>(R.id.editTextCep)
        val etEndereco  = findViewById<EditText>(R.id.editTextEndereco)
        val etNumero    = findViewById<EditText>(R.id.editTextNumero)
        val etBairro    = findViewById<EditText>(R.id.editTextBairro)
        val etCidade    = findViewById<EditText>(R.id.editTextCidade)
        val etEstado    = findViewById<EditText>(R.id.editTextEstado)
        val btnCadastro = findViewById<Button>(R.id.btnCadastro)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // ── ViaCEP no Cadastro ────────────────────────────────────
        etCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val cep = s.toString().trim()
                if (cep.length == 8) {
                    etEndereco.hint = "🔍 Buscando..."
                    etBairro.hint   = "🔍 Buscando..."
                    etCidade.hint   = "🔍 Buscando..."
                    etEstado.hint   = "🔍 Buscando..."

                    viaCep.buscar(cep).enqueue(object : Callback<ViaCepResp> {
                        override fun onResponse(call: Call<ViaCepResp>, response: Response<ViaCepResp>) {
                            val body = response.body()
                            if (body == null || body.erro == true) {
                                Toast.makeText(this@CadastroActivity, "CEP não encontrado", Toast.LENGTH_SHORT).show()
                                resetHints(etEndereco, etBairro, etCidade, etEstado)
                                return
                            }
                            // Preenche os campos
                            etEndereco.setText(body.logradouro ?: "")
                            etBairro.setText(body.bairro ?: "")
                            etCidade.setText(body.localidade ?: "")
                            etEstado.setText(body.uf ?: "")

                            // Foca no número para o usuário digitar
                            etNumero.requestFocus()
                        }
                        override fun onFailure(call: Call<ViaCepResp>, t: Throwable) {
                            Toast.makeText(this@CadastroActivity, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show()
                            resetHints(etEndereco, etBairro, etCidade, etEstado)
                        }
                    })
                } else {
                    // Limpa os campos se apagar o CEP
                    if (cep.length < 8) {
                        etEndereco.setText("")
                        etBairro.setText("")
                        etCidade.setText("")
                        etEstado.setText("")
                        resetHints(etEndereco, etBairro, etCidade, etEstado)
                    }
                }
            }
        })
        // ─────────────────────────────────────────────────────────

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
                nome      = nome,
                email     = email,
                senha     = senha,
                telefone  = etTelefone.text.toString().trim().ifEmpty { null },
                cep       = etCep.text.toString().trim().ifEmpty { null },
                endereco  = etEndereco.text.toString().trim().ifEmpty { null },
                numero    = etNumero.text.toString().trim().ifEmpty { null },
                bairro    = etBairro.text.toString().trim().ifEmpty { null },
                cidade    = etCidade.text.toString().trim().ifEmpty { null },
                estado    = etEstado.text.toString().trim().ifEmpty { null }
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

    private fun resetHints(
        etEndereco: EditText, etBairro: EditText,
        etCidade: EditText, etEstado: EditText
    ) {
        etEndereco.hint = "Rua / Logradouro"
        etBairro.hint   = "Bairro"
        etCidade.hint   = "Cidade"
        etEstado.hint   = "Estado (ex: SP)"
    }
}