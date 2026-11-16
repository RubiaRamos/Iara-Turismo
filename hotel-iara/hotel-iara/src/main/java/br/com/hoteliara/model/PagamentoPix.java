package br.com.hoteliara.model;

import br.com.hoteliara.exception.PagamentoRecusadoException;

public class PagamentoPix extends Pagamento {
    public PagamentoPix(Reserva reserva, double valor) throws PagamentoRecusadoException {
        super(reserva, valor);
    }

    @Override
    public void processar() throws PagamentoRecusadoException {
        // Simulacao academica: nao movimenta dinheiro em um banco.
        aprovar();
    }
}

