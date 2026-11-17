package br.com.hoteliara;

import java.time.LocalDate;

import br.com.hoteliara.model.Cliente;
import br.com.hoteliara.model.Pessoa;
import br.com.hoteliara.model.Quarto;
import br.com.hoteliara.model.Reserva;

/**
 * Guarda o que uma tela precisa passar para a próxima: quem está logado,
 * o que foi pesquisado, o quarto escolhido e a reserva a pagar.
 * Fica no pacote principal (e não em model) porque é estado da interface,
 * não uma regra do negócio.
 */
public final class Sessao {

    private static Pessoa usuario;
    private static LocalDate checkIn;
    private static LocalDate checkOut;
    private static int hospedes = 1;
    private static Quarto quartoSelecionado;
    private static Reserva reservaSelecionada;

    private Sessao() {
    }

    public static void entrar(Pessoa pessoa) {
        usuario = pessoa;
    }

    public static void sair() {
        usuario = null;
        quartoSelecionado = null;
        reservaSelecionada = null;
        checkIn = null;
        checkOut = null;
        hospedes = 1;
    }

    public static Pessoa getUsuario() {
        return usuario;
    }

    /** Devolve o cliente logado, ou null se quem entrou for um funcionário. */
    public static Cliente getCliente() {
        return usuario instanceof Cliente cliente ? cliente : null;
    }

    public static void guardarPesquisa(LocalDate entrada, LocalDate saida, int numHospedes) {
        checkIn = entrada;
        checkOut = saida;
        hospedes = numHospedes;
    }

    public static LocalDate getCheckIn() { return checkIn; }
    public static LocalDate getCheckOut() { return checkOut; }
    public static int getHospedes() { return hospedes; }

    public static Quarto getQuartoSelecionado() { return quartoSelecionado; }
    public static void setQuartoSelecionado(Quarto quarto) { quartoSelecionado = quarto; }

    public static Reserva getReservaSelecionada() { return reservaSelecionada; }
    public static void setReservaSelecionada(Reserva reserva) { reservaSelecionada = reserva; }
}
