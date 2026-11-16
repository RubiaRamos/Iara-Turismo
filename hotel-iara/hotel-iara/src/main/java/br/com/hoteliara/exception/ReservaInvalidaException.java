package br.com.hoteliara.exception;

/**
 * Lançada quando os dados de uma reserva não fazem sentido: check-out antes do
 * check-in, check-in no passado ou mais hóspedes do que o quarto comporta.
 */
public class ReservaInvalidaException extends Exception {

    public ReservaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
