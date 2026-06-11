package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applogintest.util.SessionManager

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val tvEmail      = findViewById<TextView>(R.id.tvAdminEmail)
        val tvVersao     = findViewById<TextView>(R.id.tvVersao)
        val tvAmbiente   = findViewById<TextView>(R.id.tvAmbiente)
        val btnVoltar    = findViewById<Button>(R.id.btnVoltarAdmin)
        val btnLogout    = findViewById<Button>(R.id.btnSair)
        val switchNotif  = findViewById<Switch>(R.id.switchNotificacoes)
        val switchEscuro = findViewById<Switch>(R.id.switchModoEscuro)
        val btnVoltarImg = findViewById<ImageButton>(R.id.btnVoltar)

        // Informações do usuário
        tvEmail.text    = SessionManager.getEmail(this) ?: "—"
        tvVersao.text   = "Versão: 1.0.0"
        tvAmbiente.text = "Ambiente: Desenvolvimento 🌱"

        // Recupera preferência salva
        val prefs = getSharedPreferences("pura_prefs", MODE_PRIVATE)
        val modoEscuroSalvo = prefs.getBoolean("modo_escuro", false)
        switchEscuro.isChecked = modoEscuroSalvo

        // Switch Modo Escuro
        switchEscuro.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("modo_escuro", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(this, "Modo escuro ativado 🌙", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(this, "Modo claro ativado ☀️", Toast.LENGTH_SHORT).show()
            }
        }

        // Switch Notificações
        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Notificações ativadas 🔔" else "Notificações desativadas"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        btnVoltar.setOnClickListener { finish() }
        btnVoltarImg.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}