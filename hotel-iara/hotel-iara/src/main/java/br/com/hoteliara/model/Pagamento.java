package br.com.hoteliara.model;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import br.com.hoteliara.exception.PagamentoRecusadoException;

// Abstrata: os dados sao comuns, mas cada forma tem seu processamento.
public abstract class Pagamento implements Cancelavel {
    private int id;
    private final Reserva reserva;
    private final double valor;
    private LocalDate data;
    private StatusPagamento status;

    protected Pagamento(Reserva reserva, double valor) throws PagamentoRecusadoException {
        if (reserva == null) throw new IllegalArgumentException("Reserva e obrigatoria.");
        if (!Double.isFinite(valor) || valor <= 0)
            throw new PagamentoRecusadoException("O valor deve ser positivo e finito.");
        this.reserva = reserva;
        this.valor = valor;
        this.data = LocalDate.now();
        this.status = StatusPagamento.PENDENTE;
    }

    public abstract void processar() throws PagamentoRecusadoException;

    protected final void validarProcessamento() throws PagamentoRecusadoException {
        if (status != StatusPagamento.PENDENTE)
            throw new IllegalStateException("Somente pagamentos pendentes podem ser processados.");
        if (reserva.getStatus() == StatusReserva.CANCELADA)
            throw new PagamentoRecusadoException("Nao e possivel pagar uma reserva cancelada.");
        // O saldo double pode conter residuos binarios; dinheiro e comparado em centavos.
        BigDecimal saldo = BigDecimal.valueOf(reserva.getSaldoDevedor())
                .setScale(2, RoundingMode.HALF_UP);
        if (BigDecimal.valueOf(valor).compareTo(saldo) > 0)
            throw new PagamentoRecusadoException("O valor ultrapassa o saldo devedor da reserva.");
    }

    // As subclasses aprovam pela operacao controlada, sem expor setter de status.
    protected final void aprovar() throws PagamentoRecusadoException {
        validarProcessamento();
        status = StatusPagamento.APROVADO;
        data = LocalDate.now();
    }

    public final void estornar() {
        if (status != StatusPagamento.APROVADO)
            throw new IllegalStateException("Somente pagamentos aprovados podem ser estornados.");
        status = StatusPagamento.ESTORNADO;
    }

    @Override
    public final void cancelar() {
        estornar();
    }

    // Nao ha taxa definida pela equipe; nao inventamos cobrancas.
    public double calcularTaxa() {
        return 0.0;
    }

    // Restauracao nao processa nem associa: o DAO faz a associacao uma unica vez.
    public static Pagamento restaurar(int id, Reserva reserva, String forma, double valor,
            LocalDate data, StatusPagamento status, Integer parcelas, LocalDate vencimento)
            throws PagamentoRecusadoException {
        if (status == null) throw new IllegalArgumentException("Status e obrigatorio.");
        if (forma == null) throw new IllegalArgumentException("Forma e obrigatoria.");
        Pagamento pagamento = switch (forma) {
            case "PIX" -> new PagamentoPix(reserva, valor);
            case "CARTAO" -> {
                if (parcelas == null) throw new IllegalArgumentException("Parcelas ausentes.");
                yield new PagamentoCartao(reserva, valor, parcelas);
            }
            case "BOLETO" -> new PagamentoBoleto(reserva, valor, vencimento);
            default -> throw new IllegalArgumentException("Forma de pagamento desconhecida.");
        };
        pagamento.id = id;
        pagamento.data = data;
        pagamento.status = status;
        return pagamento;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Reserva getReserva() { return reserva; }
    public double getValor() { return valor; }
    public LocalDate getData() { return data; }
    public StatusPagamento getStatus() { return status; }
}

