package br.com.hoteliara.model;

import java.time.LocalDate;

import br.com.hoteliara.exception.QuartoIndisponivelException;

/**
 * Contrato de algo que pode ser reservado por um período.
 *
 * Hoje só Quarto implementa, mas se o hotel passar a alugar um salão de
 * eventos, basta a nova classe implementar esta interface: o resto do
 * sistema já sabe lidar com ela.
 */
public interface Reservavel {

    /** Diz se está livre entre checkIn (inclusive) e checkOut (exclusive). */
    boolean estaDisponivel(LocalDate checkIn, LocalDate checkOut);

    /** Registra a reserva; lança exceção se o período já estiver ocupado. */
    void reservar(Reserva reserva) throws QuartoIndisponivelException;

    /** Libera o período de uma reserva cancelada ou concluída. */
    void liberar(Reserva reserva);
}
