package com.example.applogintest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applogin.model.Usuario
import com.example.applogin.network.ApiClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CadastroActivity : AppCompatActivity() {
    private val apiService = ApiClient.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cadastro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var nome =findViewById<EditText >(R.id.editTextNome)
        var email=findViewById<EditText>(R.id.editTextEmail)
        var telefone=findViewById<EditText>(R.id.editTextTelefone)
        var senha=findViewById<EditText>(R.id.editTextSenha)
        var btnCadastro=findViewById<Button>(R.id.btnCadastro)
        btnCadastro.setOnClickListener {
            var usuario = Usuario(
                nome = nome.text.toString(),
                email = email.text.toString(),
                telefone = telefone.text.toString(),
                senha = senha.text.toString())
            cadastrarUsurio(usuario)

        }

    }
    private fun cadastrarUsurio(usuario: Usuario) {
        apiService.criarUsuario(usuario).enqueue(object : Callback<Usuario> {

            override fun onResponse(call: Call<Usuario>, response: Response<Usuario>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        this@CadastroActivity,
                        "Cadastrado com Sucesso",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this@CadastroActivity,
                        "Erro: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<Usuario>, t: Throwable) {
                Toast.makeText(
                    this@CadastroActivity,
                    "Falha na rede: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                println(t.message)
            }
        })

    }

}