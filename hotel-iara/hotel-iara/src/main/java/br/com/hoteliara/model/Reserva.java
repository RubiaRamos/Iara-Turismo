package br.com.hoteliara.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.hoteliara.exception.QuartoIndisponivelException;
import br.com.hoteliara.exception.ReservaInvalidaException;

/**
 * Reserva: LIGA um Cliente a um Quarto num período (1 reserva = 1 quarto).
 *
 * Encapsulamento: o status não tem setter. Ele só muda pelos métodos
 * confirmar(), cancelar() e concluir(), que verificam se a mudança é
 * permitida (ex.: não dá para confirmar uma reserva cancelada).
 */
public class Reserva implements Cancelavel {

    private int id;
    private final Cliente cliente;
    private final Quarto quarto;
    private final LocalDate dataReserva;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int numHospedes;
    private StatusReserva status;
    private final double valorTotal;
    private final List<Pagamento> pagamentos = new ArrayList<>();

    /**
     * Cria uma reserva NOVA: valida os dados, calcula o valor e já ocupa o
     * período no quarto. Se algo der errado, lança exceção e a reserva não
     * chega a existir.
     */
    public Reserva(Cliente cliente, Quarto quarto, LocalDate checkIn, LocalDate checkOut,
                   int numHospedes) throws ReservaInvalidaException, QuartoIndisponivelException {
        validar(cliente, quarto, checkIn, checkOut, numHospedes);
        if (checkIn.isBefore(LocalDate.now())) {
            throw new ReservaInvalidaException("O check-in não pode ser numa data que já passou.");
        }
        this.cliente = cliente;
        this.quarto = quarto;
        this.dataReserva = LocalDate.now();
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numHospedes = numHospedes;
        this.status = StatusReserva.PENDENTE;
        this.valorTotal = calcularTotal();
        quarto.reservar(this); // lança QuartoIndisponivelException se já estiver ocupado
    }

    /** Construtor usado por restaurar(): monta uma reserva que já existe no banco. */
    private Reserva(int id, Cliente cliente, Quarto quarto, LocalDate dataReserva,
                    LocalDate checkIn, LocalDate checkOut, int numHospedes,
                    StatusReserva status, double valorTotal) {
        this.id = id;
        this.cliente = cliente;
        this.quarto = quarto;
        this.dataReserva = dataReserva;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numHospedes = numHospedes;
        this.status = status;
        this.valorTotal = valorTotal;
    }

    /**
     * Para o DAO: recria uma reserva lida do banco. Não reaplica as regras de
     * criação (uma reserva antiga tem check-in no passado e isso é normal) e
     * mantém o valor que foi cobrado na época.
     */
    public static Reserva restaurar(int id, Cliente cliente, Quarto quarto, LocalDate dataReserva,
                                    LocalDate checkIn, LocalDate checkOut, int numHospedes,
                                    StatusReserva status, double valorTotal)
            throws QuartoIndisponivelException {
        Reserva r = new Reserva(id, cliente, quarto, dataReserva, checkIn, checkOut,
                numHospedes, status, valorTotal);
        if (r.estaAtiva()) {
            quarto.reservar(r);
        }
        return r;
    }

    private static void validar(Cliente cliente, Quarto quarto, LocalDate checkIn,
                                LocalDate checkOut, int numHospedes)
            throws ReservaInvalidaException {
        if (cliente == null || quarto == null) {
            throw new ReservaInvalidaException("Cliente e quarto são obrigatórios.");
        }
        if (checkIn == null || checkOut == null) {
            throw new ReservaInvalidaException("Informe as datas de check-in e check-out.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new ReservaInvalidaException("O check-out deve ser depois do check-in.");
        }
        if (!quarto.comporta(numHospedes)) {
            throw new ReservaInvalidaException("O quarto " + quarto.getNumero() + " comporta "
                    + quarto.getCapacidade() + " hóspede(s); foram pedidos " + numHospedes + ".");
        }
    }

    // ---------- cálculos ----------

    public long calcularNoites() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /**
     * POLIMORFISMO: a Reserva não sabe (nem precisa saber) qual é o tipo do
     * quarto. Ela só chama calcularDiaria() e cada subclasse responde com a
     * sua regra. Um tipo novo de quarto não exige mudar esta classe.
     */
    public double calcularTotal() {
        return quarto.calcularDiaria((int) calcularNoites());
    }

    /** Soma só os pagamentos aprovados (estornados e pendentes não contam). */
    public double getValorPago() {
        return pagamentos.stream()
                .filter(p -> p.getStatus() == StatusPagamento.APROVADO)
                .mapToDouble(Pagamento::getValor)
                .sum();
    }

    public double getSaldoDevedor() {
        return Math.max(0, valorTotal - getValorPago());
    }

    public boolean estaQuitada() {
        return getSaldoDevedor() == 0;
    }

    /** Verdadeiro se o período desta reserva cruza com o período informado. */
    public boolean sobrepoe(LocalDate outroCheckIn, LocalDate outroCheckOut) {
        // Duas faixas se cruzam quando cada uma começa antes da outra terminar.
        // O dia do check-out é livre: quem sai dia 10 não bloqueia quem entra dia 10.
        return checkIn.isBefore(outroCheckOut) && outroCheckIn.isBefore(checkOut);
    }

    public boolean estaAtiva() {
        return status == StatusReserva.PENDENTE || status == StatusReserva.CONFIRMADA;
    }

    // ---------- mudanças de status ----------

    public void confirmar() {
        if (status != StatusReserva.PENDENTE) {
            throw new IllegalStateException("Só é possível confirmar uma reserva pendente.");
        }
        status = StatusReserva.CONFIRMADA;
    }

    @Override
    public void cancelar() {
        if (!estaAtiva()) {
            throw new IllegalStateException("Esta reserva já está " + status + ".");
        }
        status = StatusReserva.CANCELADA;
        quarto.liberar(this);
    }

    public void concluir() {
        if (status != StatusReserva.CONFIRMADA) {
            throw new IllegalStateException("Só é possível concluir uma reserva confirmada.");
        }
        status = StatusReserva.CONCLUIDA;
        quarto.liberar(this);
    }

    // ---------- pagamentos ----------

    public void adicionarPagamento(Pagamento pagamento) {
        if (pagamento == null) {
            throw new IllegalArgumentException("Pagamento não pode ser nulo.");
        }
        pagamentos.add(pagamento);
    }

    public List<Pagamento> getPagamentos() {
        return Collections.unmodifiableList(pagamentos);
    }

    // ---------- getters ----------

    public int getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Quarto getQuarto() { return quarto; }
    public Hotel getHotel() { return quarto.getHotel(); }
    public LocalDate getDataReserva() { return dataReserva; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getNumHospedes() { return numHospedes; }
    public StatusReserva getStatus() { return status; }
    public double getValorTotal() { return valorTotal; }

    /** O DAO usa para colocar o id gerado pelo banco. */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Reserva #%d - %s, quarto %d, %s a %s (%d noites) - R$ %.2f - %s",
                id, getHotel().getNome(), quarto.getNumero(), checkIn, checkOut,
                calcularNoites(), valorTotal, status);
    }
}
