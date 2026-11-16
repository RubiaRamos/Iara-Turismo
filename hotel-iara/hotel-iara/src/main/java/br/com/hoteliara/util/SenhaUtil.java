package br.com.hoteliara.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Transforma a senha num "hash": um código que não dá para desfazer.
 * Assim o sistema (e o banco) nunca guarda a senha de verdade; no login,
 * gera o hash da senha digitada e compara com o guardado.
 */
public final class SenhaUtil {

    private SenhaUtil() {
    }

    /** Valida a senha (mínimo 6 caracteres) e devolve o hash SHA-256 dela. */
    public static String gerarHash(String senha) {
        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter ao menos 6 caracteres");
        }
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest(senha.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
