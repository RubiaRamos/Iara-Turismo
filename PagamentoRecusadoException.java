package br.com.hoteliara.exception;

// Checked: quem processa o pagamento precisa tratar ou declarar a recusa.
public class PagamentoRecusadoException extends Exception {

    public PagamentoRecusadoException(String mensagem) {
        super(mensagem);
    }
}
