@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ViaCepResponse(
    val logradouro: String?,
    val bairro: String?,
    val localidade: String?,
    val uf: String?,
    val erro: Boolean? = false
)

interface ViaCepService {
    @GET("{cep}/json/")
    fun buscarCep(@Path("cep") cep: String): Call<ViaCepResponse>
}

class EnderecoActivity : AppCompatActivity() {

    private var freteValor = 0.0
    private var enderecoFinal = ""

    private val viaCepService: ViaCepService by lazy {
        Retrofit.Builder()
            .baseUrl("https://viacep.com.br/ws/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ViaCepService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_endereco)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val rgEndereco         = findViewById<RadioGroup>(R.id.rgEndereco)
        val rbCadastro         = findViewById<RadioButton>(R.id.rbEnderecoCadastro)
        val rbNovo             = findViewById<RadioButton>(R.id.rbNovoEndereco)
        val layoutCadastro     = findViewById<View>(R.id.layoutEnderecoCadastro)
        val layoutNovo         = findViewById<View>(R.id.layoutNovoEndereco)
        val tvEnderecoCadastro = findViewById<TextView>(R.id.tvEnderecoCadastro)
        val etCep              = findViewById<EditText>(R.id.etCep)
        val etRua              = findViewById<EditText>(R.id.etRua)
        val etNumero           = findViewById<EditText>(R.id.etNumero)
        val etBairro           = findViewById<EditText>(R.id.etBairro)
        val etCidade           = findViewById<EditText>(R.id.etCidade)
        val etEstado           = findViewById<EditText>(R.id.etEstado)
        val etComplemento      = findViewById<EditText>(R.id.etComplemento)
        val btnCalcularFrete   = findViewById<Button>(R.id.btnCalcularFrete)
        val tvFrete            = findViewById<TextView>(R.id.tvFrete)
        val btnProsseguir      = findViewById<Button>(R.id.btnProsseguirPagamento)

        // Endereço do cadastro
        if (SessionManager.temEnderecoCompleto(this)) {
            tvEnderecoCadastro.text =
                "CEP: ${SessionManager.getCep(this)}\n" +
                        "${SessionManager.getEndereco(this)}, Nº ${SessionManager.getNumero(this)}\n" +
                        "${SessionManager.getBairro(this)} - ${SessionManager.getCidade(this)}/${SessionManager.getEstado(this)}"
            rbCadastro.isEnabled = true
        } else {
            tvEnderecoCadastro.text = "Nenhum endereço cadastrado.\nUse a opção abaixo."
            rbCadastro.isEnabled = false
            rbNovo.isChecked = true
            layoutNovo.visibility = View.VISIBLE
        }

        // Alterna layouts
        rgEndereco.setOnCheckedChangeListener { _, checkedId ->
            freteValor = 0.0
            tvFrete.visibility = View.GONE
            when (checkedId) {
                R.id.rbEnderecoCadastro -> {
                    layoutCadastro.visibility = View.VISIBLE
                    layoutNovo.visibility = View.GONE
                }
                R.id.rbNovoEndereco -> {
                    layoutCadastro.visibility = View.GONE
                    layoutNovo.visibility = View.VISIBLE
                }
            }
        }

        // ViaCEP — preenche campos ao digitar 8 dígitos
        etCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val cep = s.toString().trim()
                if (cep.length == 8) {
                    etRua.hint    = "🔍 Buscando..."
                    etBairro.hint = "🔍 Buscando..."
                    etCidade.hint = "🔍 Buscando..."
                    etEstado.hint = "🔍 Buscando..."

                    viaCepService.buscarCep(cep).enqueue(object : Callback<ViaCepResponse> {
                        override fun onResponse(call: Call<ViaCepResponse>, response: Response<ViaCepResponse>) {
                            val body = response.body()
                            if (body == null || body.erro == true) {
                                Toast.makeText(this@EnderecoActivity, "CEP não encontrado", Toast.LENGTH_SHORT).show()
                                resetHints(etRua, etBairro, etCidade, etEstado)
                                return
                            }
                            etRua.setText(body.logradouro ?: "")
                            etBairro.setText(body.bairro ?: "")
                            etCidade.setText(body.localidade ?: "")
                            etEstado.setText(body.uf ?: "")
                            // Foca no número
                            etNumero.requestFocus()
                        }
                        override fun onFailure(call: Call<ViaCepResponse>, t: Throwable) {
                            Toast.makeText(this@EnderecoActivity, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show()
                            resetHints(etRua, etBairro, etCidade, etEstado)
                        }
                    })
                } else {
                    etRua.setText("")
                    etBairro.setText("")
                    etCidade.setText("")
                    etEstado.setText("")
                    resetHints(etRua, etBairro, etCidade, etEstado)
                }
            }
        })

        // Calcular frete
        btnCalcularFrete.setOnClickListener {
            if (rgEndereco.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Selecione um endereço de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cep = if (rbCadastro.isChecked) SessionManager.getCep(this)
            else etCep.text.toString().trim()

            if (rbNovo.isChecked) {
                if (cep.length < 8) {
                    Toast.makeText(this, "Digite um CEP válido (8 dígitos)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (etNumero.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Digite o número do endereço", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            freteValor = calcularFrete(cep)

            enderecoFinal = if (rbNovo.isChecked)
                "CEP: $cep - ${etRua.text}, Nº ${etNumero.text}, ${etBairro.text} - ${etCidade.text}/${etEstado.text} ${etComplemento.text}"
            else
                tvEnderecoCadastro.text.toString()

            tvFrete.text = "📦 Frete: R$ %.2f".format(freteValor)
            tvFrete.visibility = View.VISIBLE
            Toast.makeText(this, "Frete calculado com sucesso!", Toast.LENGTH_SHORT).show()
        }

        // Prosseguir
        btnProsseguir.setOnClickListener {
            if (rgEndereco.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Selecione um endereço de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (freteValor == 0.0) {
                Toast.makeText(this, "Calcule o frete antes de prosseguir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, PagamentoActivity::class.java)
            intent.putExtra("frete", freteValor)
            intent.putExtra("endereco", enderecoFinal)
            startActivity(intent)
        }
    }

    private fun resetHints(etRua: EditText, etBairro: EditText, etCidade: EditText, etEstado: EditText) {
        etRua.hint    = "Rua / Logradouro"
        etBairro.hint = "Bairro"
        etCidade.hint = "Cidade"
        etEstado.hint = "Estado (ex: SP)"
    }

    private fun calcularFrete(cep: String): Double = when {
        cep.startsWith("0") || cep.startsWith("1") -> 15.90
        cep.startsWith("2") || cep.startsWith("3") -> 22.90
        cep.startsWith("4") || cep.startsWith("5") -> 25.90
        else -> 29.90
    }
}