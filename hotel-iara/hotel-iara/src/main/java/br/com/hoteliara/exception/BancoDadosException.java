package br.com.hoteliara.exception;

// Falhas tecnicas sao repassadas sem alterar o contrato Dao<T>.
public class BancoDadosException extends RuntimeException {

    public BancoDadosException(String mensagem, Throwable causa) {
        // Preserva o erro original para permitir o diagnostico da falha.
        super(mensagem, causa);
    }
}
