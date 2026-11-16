package br.com.hoteliara.model;

import java.time.LocalDate;
import br.com.hoteliara.exception.PagamentoRecusadoException;

public class PagamentoBoleto extends Pagamento {
    private final LocalDate dataVencimento;

    public PagamentoBoleto(Reserva reserva, double valor) throws PagamentoRecusadoException {
        this(reserva, valor, LocalDate.now().plusDays(3));
    }

    public PagamentoBoleto(Reserva reserva, double valor, LocalDate dataVencimento)
            throws PagamentoRecusadoException {
        super(reserva, valor);
        // Datas passadas sao permitidas para restaurar boletos antigos do banco.
        if (dataVencimento == null)
            throw new IllegalArgumentException("Vencimento e obrigatorio.");
        this.dataVencimento = dataVencimento;
    }

    @Override
    public void processar() throws PagamentoRecusadoException {
        validarProcessamento();
        // Emitir um boleto nao comprova que ele foi pago.
    }

    public void registrarBaixa() throws PagamentoRecusadoException {
        // Reconfere o saldo: outro pagamento pode ter ocorrido apos a emissao.
        aprovar();
    }

    public LocalDate getDataVencimento() { return dataVencimento; }
}

