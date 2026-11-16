package br.com.hoteliara.model;

import br.com.hoteliara.exception.PagamentoRecusadoException;

public class PagamentoCartao extends Pagamento {
    private final int parcelas;

    public PagamentoCartao(Reserva reserva, double valor, int parcelas)
            throws PagamentoRecusadoException {
        super(reserva, valor);
        if (parcelas < 1 || parcelas > 12)
            throw new PagamentoRecusadoException("O cartao permite de 1 a 12 parcelas.");
        this.parcelas = parcelas;
    }

    @Override
    public void processar() throws PagamentoRecusadoException {
        aprovar();
    }

    public int getParcelas() { return parcelas; }
}

