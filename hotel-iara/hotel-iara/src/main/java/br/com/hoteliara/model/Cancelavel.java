package br.com.hoteliara.model;

/**
 * Contrato para tudo o que pode ser cancelado.
 *
 * Reserva e Pagamento não têm parentesco (um não é um tipo do outro), por isso
 * não faz sentido usar herança. A interface permite tratar os dois do mesmo
 * jeito: qualquer código que receba um Cancelavel pode chamar cancelar().
 */
public interface Cancelavel {

    void cancelar();
}
