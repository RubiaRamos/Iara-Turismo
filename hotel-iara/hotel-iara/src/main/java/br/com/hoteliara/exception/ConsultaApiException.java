package br.com.hoteliara.exception;

// Checked: a consulta pode falhar mesmo quando o usuario fornece dados validos.
public class ConsultaApiException extends Exception {
    public ConsultaApiException(String mensagem) {
        super(mensagem);
    }

    public ConsultaApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
