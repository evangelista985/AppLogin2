@file:Suppress("SpellCheckingInspection")
package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.applogintest.model.CarrinhoManager
import com.example.applogintest.util.SessionManager

class EnderecoActivity : AppCompatActivity() {

    private var freteValor = 0.0
    private var enderecoFinal = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_endereco)

        findViewById<ImageButton>(R.id.btnVoltar).setOnClickListener { finish() }

        val rgEndereco           = findViewById<RadioGroup>(R.id.rgEndereco)
        val rbCadastro           = findViewById<RadioButton>(R.id.rbEnderecoCadastro)
        val rbNovo               = findViewById<RadioButton>(R.id.rbNovoEndereco)
        val layoutCadastro       = findViewById<View>(R.id.layoutEnderecoCadastro)
        val layoutNovo           = findViewById<View>(R.id.layoutNovoEndereco)
        val tvEnderecoCadastro   = findViewById<TextView>(R.id.tvEnderecoCadastro)
        val etCep                = findViewById<EditText>(R.id.etCep)
        val etNumero             = findViewById<EditText>(R.id.etNumero)
        val etComplemento        = findViewById<EditText>(R.id.etComplemento)
        val btnCalcularFrete     = findViewById<Button>(R.id.btnCalcularFrete)
        val tvFrete              = findViewById<TextView>(R.id.tvFrete)
        val btnProsseguir        = findViewById<Button>(R.id.btnProsseguirPagamento)

        // Preenche endereço do cadastro
        val temEndereco = SessionManager.temEnderecoCompleto(this)
        if (temEndereco) {
            val cep      = SessionManager.getCep(this)
            val rua      = SessionManager.getEndereco(this)
            val numero   = SessionManager.getNumero(this)
            val bairro   = SessionManager.getBairro(this)
            val cidade   = SessionManager.getCidade(this)
            val estado   = SessionManager.getEstado(this)
            tvEnderecoCadastro.text =
                "CEP: $cep\n$rua, Nº $numero\n$bairro - $cidade/$estado"
            rbCadastro.isEnabled = true
        } else {
            tvEnderecoCadastro.text = "Nenhum endereço cadastrado.\nUse a opção abaixo."
            rbCadastro.isEnabled = false
            rbNovo.isChecked = true
            layoutNovo.visibility = View.VISIBLE
        }

        // Alterna entre os layouts
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

        // Calcular frete
        btnCalcularFrete.setOnClickListener {
            if (rgEndereco.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Selecione um endereço de entrega", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cep = when {
                rbCadastro.isChecked -> SessionManager.getCep(this)
                else -> etCep.text.toString().trim()
            }

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

            // Simulação de frete por região
            freteValor = when {
                cep.startsWith("0") || cep.startsWith("1") -> 15.90
                cep.startsWith("2") || cep.startsWith("3") -> 22.90
                cep.startsWith("4") || cep.startsWith("5") -> 25.90
                else -> 29.90
            }

            enderecoFinal = if (rbNovo.isChecked) {
                "CEP: $cep, Nº: ${etNumero.text}, ${etComplemento.text}"
            } else {
                tvEnderecoCadastro.text.toString()
            }

            tvFrete.text = "📦 Frete: R$ %.2f".format(freteValor)
            tvFrete.visibility = View.VISIBLE
            Toast.makeText(this, "Frete calculado com sucesso!", Toast.LENGTH_SHORT).show()
        }

        // Prosseguir para pagamento
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
}
