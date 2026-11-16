package br.com.hoteliara.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.hoteliara.exception.QuartoIndisponivelException;

/**
 * Quarto de um hotel.
 *
 * É ABSTRATA porque não existe um "quarto genérico": todo quarto é Padrão,
 * Executivo, Luxo ou Suíte. A parte comum (número, andar, disponibilidade)
 * fica aqui; o que muda de um tipo para outro (o cálculo do valor) é um
 * método abstrato que cada subclasse implementa.
 */
public abstract class Quarto implements Reservavel {

    private int id;
    private int numero;
    private int andar;
    private boolean emManutencao;
    private final Categoria categoria;
    private final Hotel hotel;

    // Reservas ativas deste quarto. É com elas que se descobre se o quarto
    // está livre num período (duas reservas não podem se sobrepor).
    private final List<Reserva> reservas = new ArrayList<>();

    /**
     * @param tipoEsperado passado por cada subclasse, para impedir, por
     *                     exemplo, um QuartoLuxo com categoria do tipo PADRAO
     */
    protected Quarto(int id, int numero, int andar, Categoria categoria, Hotel hotel,
                     TipoQuarto tipoEsperado) {
        if (categoria == null || hotel == null) {
            throw new IllegalArgumentException("Categoria e hotel são obrigatórios.");
        }
        if (categoria.getTipo() != tipoEsperado) {
            throw new IllegalArgumentException("Categoria do tipo " + categoria.getTipo()
                    + " não serve para um quarto " + tipoEsperado + ".");
        }
        if (numero <= 0) {
            throw new IllegalArgumentException("Número do quarto deve ser positivo.");
        }
        if (andar < 0) {
            throw new IllegalArgumentException("Andar não pode ser negativo.");
        }
        this.id = id;
        this.numero = numero;
        this.andar = andar;
        this.categoria = categoria;
        this.hotel = hotel;
    }

    /**
     * Cria a subclasse certa a partir do tipo da categoria. É o que o DAO usa
     * ao ler o banco: a coluna "tipo" diz qual classe instanciar.
     */
    public static Quarto criar(int id, int numero, int andar, Categoria categoria, Hotel hotel) {
        return switch (categoria.getTipo()) {
            case PADRAO -> new QuartoPadrao(id, numero, andar, categoria, hotel);
            case EXECUTIVO -> new QuartoExecutivo(id, numero, andar, categoria, hotel);
            case LUXO -> new QuartoLuxo(id, numero, andar, categoria, hotel);
            case SUITE -> new Suite(id, numero, andar, categoria, hotel);
        };
    }

    /**
     * MÉTODO ABSTRATO (polimorfismo): valor total das diárias para a
     * quantidade de noites. Cada tipo de quarto calcula do seu jeito.
     */
    public abstract double calcularDiaria(int noites);

    /** Valor base de uma noite, vindo da categoria. As subclasses partem dele. */
    protected double getValorBase() {
        return categoria.getValorDiaria();
    }

    // ---------- Reservavel ----------

    @Override
    public boolean estaDisponivel(LocalDate checkIn, LocalDate checkOut) {
        if (emManutencao) {
            return false;
        }
        // Disponível se NENHUMA reserva ativa se sobrepõe ao período.
        return reservas.stream()
                .filter(Reserva::estaAtiva)
                .noneMatch(r -> r.sobrepoe(checkIn, checkOut));
    }

    @Override
    public void reservar(Reserva reserva) throws QuartoIndisponivelException {
        if (emManutencao) {
            throw new QuartoIndisponivelException("O quarto " + numero + " está em manutenção.");
        }
        if (!estaDisponivel(reserva.getCheckIn(), reserva.getCheckOut())) {
            throw new QuartoIndisponivelException("O quarto " + numero
                    + " já está reservado nesse período.");
        }
        reservas.add(reserva);
    }

    @Override
    public void liberar(Reserva reserva) {
        reservas.remove(reserva);
    }

    // ---------- situação do quarto ----------

    /**
     * A situação não fica guardada num atributo que qualquer um pode trocar:
     * ela é calculada. Em manutenção -> MANUTENCAO; tem reserva confirmada
     * cobrindo hoje -> OCUPADO; senão -> DISPONIVEL.
     */
    public StatusQuarto getStatus() {
        if (emManutencao) {
            return StatusQuarto.MANUTENCAO;
        }
        LocalDate hoje = LocalDate.now();
        boolean ocupadoHoje = reservas.stream()
                .anyMatch(r -> r.getStatus() == StatusReserva.CONFIRMADA
                        && r.sobrepoe(hoje, hoje.plusDays(1)));
        return ocupadoHoje ? StatusQuarto.OCUPADO : StatusQuarto.DISPONIVEL;
    }

    public void colocarEmManutencao() {
        emManutencao = true;
    }

    public void retirarDaManutencao() {
        emManutencao = false;
    }

    public boolean comporta(int hospedes) {
        return hospedes >= 1 && hospedes <= getCapacidade();
    }

    // ---------- getters ----------

    public int getId() { return id; }
    public int getNumero() { return numero; }
    public int getAndar() { return andar; }
    public Categoria getCategoria() { return categoria; }
    public Hotel getHotel() { return hotel; }
    public TipoQuarto getTipo() { return categoria.getTipo(); }
    public int getCapacidade() { return categoria.getCapacidade(); }

    /** Cópia só de leitura: ninguém de fora adiciona reserva sem passar por reservar(). */
    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    /** O DAO usa para colocar o id gerado pelo banco. */
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("Quarto %d (%s, %dº andar) - %s",
                numero, categoria.getNome(), andar, getStatus());
    }
}
