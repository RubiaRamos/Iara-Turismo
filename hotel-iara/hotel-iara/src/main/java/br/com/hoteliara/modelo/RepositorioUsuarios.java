package br.com.hoteliara.modelo;

import java.util.HashMap;
import java.util.Map;

/**
 * Guarda os usuários em memória (somem quando o programa fecha).
 * Serve só para demonstrar o fluxo cadastro -> login; para um sistema real,
 * troque por banco de dados e nunca guarde senha em texto puro.
 */
public final class RepositorioUsuarios {

    private static final RepositorioUsuarios INSTANCIA = new RepositorioUsuarios();

    private final Map<String, String> usuarios = new HashMap<>();

    private RepositorioUsuarios() {
        // Usuário de teste para entrar sem precisar cadastrar.
        usuarios.put("teste@hoteliara.com", "123456");
    }

    public static RepositorioUsuarios getInstancia() {
        return INSTANCIA;
    }

    /** @return false se o e-mail já existir. */
    public boolean cadastrar(String email, String senha) {
        String chave = email.trim().toLowerCase();
        if (usuarios.containsKey(chave)) {
            return false;
        }
        usuarios.put(chave, senha);
        return true;
    }

    public boolean autenticar(String email, String senha) {
        String esperada = usuarios.get(email.trim().toLowerCase());
        return esperada != null && esperada.equals(senha);
    }
}
