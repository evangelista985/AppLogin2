package com.example.applogintest

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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

        val tvEmail     = findViewById<TextView>(R.id.tvAdminEmail)
        val tvVersao    = findViewById<TextView>(R.id.tvVersao)
        val tvAmbiente  = findViewById<TextView>(R.id.tvAmbiente)
        val btnVoltar   = findViewById<Button>(R.id.btnVoltarAdmin)
        val btnLogout   = findViewById<Button>(R.id.btnSair)
        val switchNotif = findViewById<Switch>(R.id.switchNotificacoes)

        // Informações do usuário logado
        tvEmail.text    = "Usuário: ${SessionManager.getEmail(this)}"
        tvVersao.text   = "Versão: 1.0.0"
        tvAmbiente.text = "Ambiente: Desenvolvimento 🌱"

        switchNotif.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "Notificações ativadas" else "Notificações desativadas"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        btnVoltar.setOnClickListener { finish() }

        btnLogout.setOnClickListener {
            SessionManager.logout(this)
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
