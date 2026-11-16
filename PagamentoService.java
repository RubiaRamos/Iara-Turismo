package br.com.hoteliara.service;

import br.com.hoteliara.exception.PagamentoRecusadoException;
import br.com.hoteliara.model.Pagamento;

public class PagamentoService {
    public void processar(Pagamento pagamento) throws PagamentoRecusadoException {
        if (pagamento == null) throw new IllegalArgumentException("Pagamento e obrigatorio.");
        if (pagamento.getReserva().getPagamentos().contains(pagamento))
            throw new IllegalStateException("Este pagamento ja esta associado a reserva.");

        // Polimorfismo: nao precisamos testar se e Pix, cartao ou boleto.
        pagamento.processar();
        // O construtor nao altera a reserva. Associamos somente apos o processamento.
        pagamento.getReserva().adicionarPagamento(pagamento);
    }
}

