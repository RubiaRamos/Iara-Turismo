package br.com.hoteliara.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.hoteliara.exception.QuartoIndisponivelException;
import br.com.hoteliara.exception.ReservaInvalidaException;

public class Cliente extends Pessoa {
    private final List<Reserva> reservas = new ArrayList<>();

    public Cliente(int id, String nome, String cpf, String email,
                   String senhaHash, String telefone, LocalDate dataNascimento) {
        super(id, nome, cpf, email, senhaHash, telefone, dataNascimento);
    }

    @Override
    public String exibirDados() {
        return "Cliente: " + getNome() + " | " + getEmail()
                + " | Reservas: " + reservas.size();
    }

    /**
     * Cria a reserva e guarda no histórico. As validações (datas, capacidade,
     * quarto livre) ficam na própria Reserva, que lança exceção se algo
     * estiver errado; aqui só repassamos para a tela avisar o usuário.
     */
    public Reserva realizarReserva(Quarto quarto, LocalDate checkIn, LocalDate checkOut,
                                   int numHospedes)
            throws ReservaInvalidaException, QuartoIndisponivelException {
        Reserva reserva = new Reserva(this, quarto, checkIn, checkOut, numHospedes);
        reservas.add(reserva);
        return reserva;
    }

    public boolean cancelarReserva(Reserva reserva) {
        if (!reservas.contains(reserva) || !reserva.estaAtiva()) {
            return false;
        }
        reserva.cancelar();
        return true;
    }

    /** Usado pelo DAO para devolver ao cliente as reservas lidas do banco. */
    public void adicionarReservaAoHistorico(Reserva reserva) {
        if (reserva.getCliente() != this)
            throw new IllegalArgumentException("Essa reserva é de outro cliente");
        if (!reservas.contains(reserva)) reservas.add(reserva);
    }

    public List<Reserva> consultarHistorico() {
        return Collections.unmodifiableList(reservas);
    }

    /** Só avalia quem se hospedou: a reserva tem que ser deste cliente e estar concluída. */
    public Avaliacao avaliarHotel(Reserva reserva, int nota, String comentario) {
        if (!reservas.contains(reserva))
            throw new IllegalArgumentException("Essa reserva não é deste cliente");
        if (reserva.getStatus() != StatusReserva.CONCLUIDA)
            throw new IllegalStateException("Só pode avaliar hotel onde já se hospedou");
        Avaliacao avaliacao = new Avaliacao(reserva, nota, comentario);
        reserva.getHotel().adicionarAvaliacao(avaliacao);
        return avaliacao;
    }
}
