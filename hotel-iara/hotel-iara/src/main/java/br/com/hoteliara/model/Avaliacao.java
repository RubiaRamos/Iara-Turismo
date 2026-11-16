package br.com.hoteliara.model;

import java.time.LocalDate;

public class Avaliacao {
    private int id;
    private final Reserva reserva;
    private int nota;
    private String comentario;
    private final LocalDate data;

    public Avaliacao(Reserva reserva, int nota, String comentario) {
        this(0, reserva, nota, comentario, LocalDate.now());
    }

    private Avaliacao(int id, Reserva reserva, int nota, String comentario, LocalDate data) {
        if (reserva == null) throw new IllegalArgumentException("Reserva e obrigatoria.");
        if (reserva.getStatus() != StatusReserva.CONCLUIDA)
            throw new IllegalStateException("Somente reservas concluidas podem ser avaliadas.");
        if (data == null) throw new IllegalArgumentException("Data e obrigatoria.");
        this.id = id;
        this.reserva = reserva;
        this.data = data;
        editar(nota, comentario);
        // Quem chama associa ao hotel; Cliente.avaliarHotel ja faz isso.
    }

    public static Avaliacao restaurar(int id, Reserva reserva, int nota,
            String comentario, LocalDate data) {
        return new Avaliacao(id, reserva, nota, comentario, data);
    }

    public void validarNota(int nota) {
        if (nota < 1 || nota > 5)
            throw new IllegalArgumentException("A nota deve estar entre 1 e 5.");
    }

    public void editar(int nota, String comentario) {
        validarNota(nota);
        if (comentario != null && comentario.length() > 500)
            throw new IllegalArgumentException("O comentario permite ate 500 caracteres.");
        // Valida tudo antes de modificar, evitando uma edicao parcial.
        this.nota = nota;
        this.comentario = comentario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Reserva getReserva() { return reserva; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }
    public LocalDate getData() { return data; }
}

