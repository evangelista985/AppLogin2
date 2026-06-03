@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.AtualizarEnderecoRequest
import com.example.applogintest.network.ApiClient
import com.example.applogintest.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

// ── ViaCEP ────────────────────────────────────────────────
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

// ── Atualizar endereço no backend ─────────────────────────


interface ClienteService {
    @PUT("api/clientes/endereco")
    fun atualizarEndereco(
        @Header("Authorization") token: String,
        @Body body: AtualizarEnderecoRequest
    ): Call<Map<String, String>>
}
// ─────────────────────────────────────────────────────────

enum class OpcaoEndereco { CADASTRO, TEMPORARIO, ATUALIZAR, NENHUMA }

class EnderecoActivity : AppCompatActivity() {

    private var opcaoSelecionada = OpcaoEndereco.NENHUMA
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

        // Views — opções
        val optCadastro   = findViewById<LinearLayout>(R.id.optCadastro)
        val optTemporario = findViewById<LinearLayout>(R.id.optTemporario)
        val optAtualizar  = findViewById<LinearLayout>(R.id.optAtualizar)

        // Views — radio dots
        val radioCadastro   = findViewById<View>(R.id.radioCadastro)
        val radioTemporario = findViewById<View>(R.id.radioTemporario)
        val radioAtualizar  = findViewById<View>(R.id.radioAtualizar)

        // Views — endereço salvo
        val tvEnderecoSalvo = findViewById<TextView>(R.id.tvEnderecoSalvo)

        // Views — formulário
        val layoutFormEndereco   = findViewById<LinearLayout>(R.id.layoutFormEndereco)
        val layoutFreteCadastro  = findViewById<LinearLayout>(R.id.layoutFreteCadastro)
        val infoBoxTemporario    = findViewById<LinearLayout>(R.id.infoBoxTemporario)
        val tvSecaoTag           = findViewById<TextView>(R.id.tvSecaoTag)
        val etCep                = findViewById<EditText>(R.id.etCep)
        val etRua                = findViewById<EditText>(R.id.etRua)
        val etNumero             = findViewById<EditText>(R.id.etNumero)
        val etBairro             = findViewById<EditText>(R.id.etBairro)
        val etCidade             = findViewById<EditText>(R.id.etCidade)
        val etEstado             = findViewById<EditText>(R.id.etEstado)
        val etComplemento        = findViewById<EditText>(R.id.etComplemento)
        val btnCalcularFrete     = findViewById<Button>(R.id.btnCalcularFrete)
        val tvFrete              = findViewById<TextView>(R.id.tvFrete)
        val btnCalcularFreteCad  = findViewById<Button>(R.id.btnCalcularFreteCadastro)
        val tvFreteCad           = findViewById<TextView>(R.id.tvFreteCadastro)
        val btnProsseguir        = findViewById<Button>(R.id.btnProsseguirPagamento)

        // Preenche endereço do cadastro na opção 1
        val temEndereco = SessionManager.temEnderecoCompleto(this)
        if (temEndereco) {
            tvEnderecoSalvo.text =
                "📍 ${SessionManager.getEndereco(this)}, Nº ${SessionManager.getNumero(this)}\n" +
                        "${SessionManager.getBairro(this)} — ${SessionManager.getCidade(this)}/${SessionManager.getEstado(this)}"
            optCadastro.alpha = 1f
        } else {
            tvEnderecoSalvo.text = "Nenhum endereço cadastrado"
            optCadastro.alpha = 0.5f
            optCadastro.isEnabled = false
        }

        // ── Seleção de opção ──────────────────────────────────────
        fun selecionarOpcao(opcao: OpcaoEndereco) {
            opcaoSelecionada = opcao
            freteValor = 0.0
            tvFrete.visibility = View.GONE
            tvFreteCad.visibility = View.GONE

            // Reset visual
            listOf(optCadastro, optTemporario, optAtualizar).forEach {
                it.setBackgroundResource(R.drawable.bg_opcao_inativa)
            }
            listOf(radioCadastro, radioTemporario, radioAtualizar).forEach {
                it.setBackgroundResource(R.drawable.radio_inativo)
            }

            when (opcao) {
                OpcaoEndereco.CADASTRO -> {
                    optCadastro.setBackgroundResource(R.drawable.bg_opcao_ativa)
                    radioCadastro.setBackgroundResource(R.drawable.radio_ativo)
                    layoutFormEndereco.visibility = View.GONE
                    layoutFreteCadastro.visibility = View.VISIBLE
                    infoBoxTemporario.visibility = View.GONE
                }
                OpcaoEndereco.TEMPORARIO -> {
                    optTemporario.setBackgroundResource(R.drawable.bg_opcao_ativa)
                    radioTemporario.setBackgroundResource(R.drawable.radio_ativo)
                    layoutFormEndereco.visibility = View.VISIBLE
                    layoutFreteCadastro.visibility = View.GONE
                    infoBoxTemporario.visibility = View.VISIBLE
                    tvSecaoTag.text = "Informe o endereço de entrega"
                }
                OpcaoEndereco.ATUALIZAR -> {
                    optAtualizar.setBackgroundResource(R.drawable.bg_opcao_ativa)
                    radioAtualizar.setBackgroundResource(R.drawable.radio_ativo)
                    layoutFormEndereco.visibility = View.VISIBLE
                    layoutFreteCadastro.visibility = View.GONE
                    infoBoxTemporario.visibility = View.GONE
                    tvSecaoTag.text = "Novo endereço padrão"
                }
                else -> {
                    layoutFormEndereco.visibility = View.GONE
                    layoutFreteCadastro.visibility = View.GONE
                }
            }
        }

        optCadastro.setOnClickListener   { if (temEndereco) selecionarOpcao(OpcaoEndereco.CADASTRO) }
        optTemporario.setOnClickListener { selecionarOpcao(OpcaoEndereco.TEMPORARIO) }
        optAtualizar.setOnClickListener  { selecionarOpcao(OpcaoEndereco.ATUALIZAR) }
        // ─────────────────────────────────────────────────────────

        // ── ViaCEP ────────────────────────────────────────────────
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
                            etNumero.requestFocus()
                        }
                        override fun onFailure(call: Call<ViaCepResponse>, t: Throwable) {
                            Toast.makeText(this@EnderecoActivity, "Erro ao buscar CEP", Toast.LENGTH_SHORT).show()
                            resetHints(etRua, etBairro, etCidade, etEstado)
                        }
                    })
                } else {
                    etRua.setText(""); etBairro.setText("")
                    etCidade.setText(""); etEstado.setText("")
                    resetHints(etRua, etBairro, etCidade, etEstado)
                }
            }
        })
        // ─────────────────────────────────────────────────────────

        // ── Calcular frete — opção 1 (cadastro) ──────────────────
        btnCalcularFreteCad.setOnClickListener {
            val cep = SessionManager.getCep(this)
            freteValor = calcularFrete(cep)
            enderecoFinal = tvEnderecoSalvo.text.toString()
            tvFreteCad.text = "📦 Frete: R$ %.2f".format(freteValor)
            tvFreteCad.visibility = View.VISIBLE
            Toast.makeText(this, "Frete calculado!", Toast.LENGTH_SHORT).show()
        }

        // ── Calcular frete — opções 2 e 3 ────────────────────────
        btnCalcularFrete.setOnClickListener {
            val cep = etCep.text.toString().trim()
            if (cep.length < 8) {
                Toast.makeText(this, "Digite um CEP válido (8 dígitos)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (etNumero.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Digite o número do endereço", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Se for opção 3 (atualizar), salva no backend
            if (opcaoSelecionada == OpcaoEndereco.ATUALIZAR) {
                salvarEnderecoNoBackend(
                    cep          = cep,
                    rua          = etRua.text.toString().trim(),
                    numero       = etNumero.text.toString().trim(),
                    bairro       = etBairro.text.toString().trim(),
                    cidade       = etCidade.text.toString().trim(),
                    estado       = etEstado.text.toString().trim(),
                    complemento  = etComplemento.text.toString().trim()
                )
            }

            freteValor = calcularFrete(cep)
            enderecoFinal = "CEP: $cep — ${etRua.text}, Nº ${etNumero.text}, ${etBairro.text} — ${etCidade.text}/${etEstado.text}"
            tvFrete.text = "📦 Frete: R$ %.2f".format(freteValor)
            tvFrete.visibility = View.VISIBLE
            Toast.makeText(this, "Frete calculado!", Toast.LENGTH_SHORT).show()
        }

        // ── Prosseguir ────────────────────────────────────────────
        btnProsseguir.setOnClickListener {
            if (opcaoSelecionada == OpcaoEndereco.NENHUMA) {
                Toast.makeText(this, "Selecione uma opção de entrega", Toast.LENGTH_SHORT).show()
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

    // ── Salva endereço padrão no backend (opção 3) ────────────────
    private fun salvarEnderecoNoBackend(
        cep: String, rua: String, numero: String,
        bairro: String, cidade: String, estado: String, complemento: String
    ) {
        val token = SessionManager.getBearerToken(this)
        val body  = AtualizarEnderecoRequest(cep, rua, numero, bairro, cidade, estado, complemento)

        ApiClient.instance.atualizarEndereco(token, body)
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                    if (response.isSuccessful) {
                        // Atualiza SessionManager localmente
                        SessionManager.salvar(
                            context  = this@EnderecoActivity,
                            token    = SessionManager.getToken(this@EnderecoActivity) ?: "",
                            id       = SessionManager.getId(this@EnderecoActivity),
                            nome     = SessionManager.getNome(this@EnderecoActivity),
                            email    = SessionManager.getEmail(this@EnderecoActivity),
                            cep      = cep,
                            endereco = rua,
                            numero   = numero,
                            bairro   = bairro,
                            cidade   = cidade,
                            estado   = estado
                        )
                        Toast.makeText(this@EnderecoActivity, "Endereço padrão atualizado!", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(this@EnderecoActivity, "Erro ao salvar endereço", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun resetHints(etRua: EditText, etBairro: EditText, etCidade: EditText, etEstado: EditText) {
        etRua.hint = "Rua / Logradouro"; etBairro.hint = "Bairro"
        etCidade.hint = "Cidade"; etEstado.hint = "Estado"
    }

    private fun calcularFrete(cep: String): Double = when {
        cep.startsWith("0") || cep.startsWith("1") -> 15.90
        cep.startsWith("2") || cep.startsWith("3") -> 22.90
        cep.startsWith("4") || cep.startsWith("5") -> 25.90
        else -> 29.90
    }
}
