package br.com.hoteliara.exception;

/**
 * Lançada quando se tenta reservar um quarto que já tem reserva no período
 * pedido ou que está em manutenção.
 *
 * É "checked" (herda de Exception): o compilador obriga quem chama a tratar,
 * porque essa situação acontece no uso normal do sistema e a tela precisa
 * avisar o usuário.
 */
public class QuartoIndisponivelException extends Exception {

    public QuartoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
