package com.example.applogintest

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applogintest.model.Usuario
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
        val etCpf       = findViewById<EditText>(R.id.editTextCpf)
        val etCep       = findViewById<EditText>(R.id.editTextCep)
        val etEndereco  = findViewById<EditText>(R.id.editTextEndereco)
        val etNumero    = findViewById<EditText>(R.id.editTextNumero)
        val etBairro    = findViewById<EditText>(R.id.editTextBairro)
        val etCidade    = findViewById<EditText>(R.id.editTextCidade)
        val etEstado    = findViewById<EditText>(R.id.editTextEstado)
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

            val usuario = Usuario(
                nome     = nome,
                email    = email,
                telefone = etTelefone.text.toString().trim(),
                senha    = senha,
                cpf      = etCpf.text.toString().trim(),
                cep      = etCep.text.toString().trim(),
                endereco = etEndereco.text.toString().trim(),
                numero   = etNumero.text.toString().trim(),
                bairro   = etBairro.text.toString().trim(),
                cidade   = etCidade.text.toString().trim(),
                estado   = etEstado.text.toString().trim()
            )

            progressBar.visibility = View.VISIBLE
            btnCadastro.isEnabled  = false

            ApiClient.instance.cadastrar(usuario).enqueue(object : Callback<Usuario> {
                override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                    progressBar.visibility = View.GONE
                    btnCadastro.isEnabled  = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@CadastroActivity, "Cadastro realizado! Faça login.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@CadastroActivity, "Erro ao cadastrar: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Usuario>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    btnCadastro.isEnabled  = true
                    Toast.makeText(this@CadastroActivity, "Falha na rede: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
