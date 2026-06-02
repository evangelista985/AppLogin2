// ── Cadastro ──────────────────────────────────────────────────────────────────
data class CadastroRequest(
    val nome: String,
    val email: String,
    val senha: String,
    val telefone: String? = null,
    val cep: String? = null,
    val endereco: String? = null,
    val numero: String? = null,
    val bairro: String? = null,
    val cidade: String? = null,
    val estado: String? = null
)