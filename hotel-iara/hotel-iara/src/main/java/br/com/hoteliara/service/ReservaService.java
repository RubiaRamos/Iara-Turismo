package br.com.hoteliara.service;

import java.time.LocalDate;

import br.com.hoteliara.dao.AvaliacaoDAO;
import br.com.hoteliara.dao.PagamentoDAO;
import br.com.hoteliara.dao.ReservaDAO;
import br.com.hoteliara.exception.PagamentoRecusadoException;
import br.com.hoteliara.exception.QuartoIndisponivelException;
import br.com.hoteliara.exception.ReservaInvalidaException;
import br.com.hoteliara.model.Avaliacao;
import br.com.hoteliara.model.Cancelavel;
import br.com.hoteliara.model.Cliente;
import br.com.hoteliara.model.Pagamento;
import br.com.hoteliara.model.PagamentoBoleto;
import br.com.hoteliara.model.Quarto;
import br.com.hoteliara.model.Reserva;
import br.com.hoteliara.model.StatusPagamento;
import br.com.hoteliara.model.StatusReserva;

/**
 * Coordena reserva, pagamento e cancelamento: chama as regras das classes
 * do modelo e grava o resultado no banco pelos DAOs.
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final PagamentoDAO pagamentoDAO = new PagamentoDAO();
    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    private final PagamentoService pagamentoService = new PagamentoService();

    /** O Cliente cria a Reserva (que valida tudo); aqui só salvamos. */
    public Reserva reservar(Cliente cliente, Quarto quarto, LocalDate checkIn, LocalDate checkOut,
                            int hospedes) throws ReservaInvalidaException, QuartoIndisponivelException {
        Reserva reserva = cliente.realizarReserva(quarto, checkIn, checkOut, hospedes);
        reservaDAO.salvar(reserva);
        return reserva;
    }

    /**
     * Processa o pagamento (polimorfismo: Pix, cartão ou boleto respondem do
     * seu jeito), grava, e confirma a reserva quando ela fica quitada.
     */
    public void pagar(Reserva reserva, Pagamento pagamento) throws PagamentoRecusadoException {
        pagamentoService.processar(pagamento);
        pagamentoDAO.salvar(pagamento);
        if (reserva.getStatus() == StatusReserva.PENDENTE && reserva.estaQuitada()) {
            reserva.confirmar();
            reservaDAO.atualizar(reserva);
        }
    }

    /**
     * Cancela a reserva e estorna os pagamentos aprovados. Reserva e Pagamento
     * não têm parentesco, mas os dois são Cancelavel: tratamos todos igual.
     */
    public void cancelar(Reserva reserva) {
        reserva.cancelar();
        reservaDAO.atualizar(reserva);
        for (Pagamento pagamento : reserva.getPagamentos()) {
            if (pagamento.getStatus() == StatusPagamento.APROVADO) {
                Cancelavel cancelavel = pagamento;
                cancelavel.cancelar();
                pagamentoDAO.atualizar(pagamento);
            }
        }
    }

    /** Só uma avaliação por reserva; quem valida "só quem se hospedou" é o Cliente. */
    public Avaliacao avaliar(Cliente cliente, Reserva reserva, int nota, String comentario) {
        boolean jaAvaliada = reserva.getHotel().getAvaliacoes().stream()
                .anyMatch(a -> a.getReserva().getId() == reserva.getId());
        if (jaAvaliada) throw new IllegalStateException("Esta reserva já foi avaliada.");
        Avaliacao avaliacao = cliente.avaliarHotel(reserva, nota, comentario);
        avaliacaoDAO.salvar(avaliacao);
        return avaliacao;
    }

    // ---------- ações do funcionário ----------

    /** O funcionário confirma a reserva (ex.: pagamento feito no balcão). */
    public void confirmar(Reserva reserva) {
        reserva.confirmar();          // a Reserva confere se está PENDENTE
        reservaDAO.atualizar(reserva);
    }

    /** Check-out feito: a reserva termina e o quarto é liberado. */
    public void concluir(Reserva reserva) {
        reserva.concluir();           // a Reserva confere se está CONFIRMADA
        reservaDAO.atualizar(reserva);
    }

    /**
     * O boleto foi pago no banco: registra a baixa do primeiro boleto pendente
     * e confirma a reserva se ela ficou quitada.
     */
    public PagamentoBoleto registrarBaixaBoleto(Reserva reserva) throws PagamentoRecusadoException {
        PagamentoBoleto boleto = boletoPendente(reserva);
        if (boleto == null)
            throw new IllegalStateException("Esta reserva não tem boleto pendente.");
        boleto.registrarBaixa();
        pagamentoDAO.atualizar(boleto);
        if (reserva.getStatus() == StatusReserva.PENDENTE && reserva.estaQuitada()) {
            reserva.confirmar();
            reservaDAO.atualizar(reserva);
        }
        return boleto;
    }

    public PagamentoBoleto boletoPendente(Reserva reserva) {
        for (Pagamento pagamento : reserva.getPagamentos()) {
            if (pagamento instanceof PagamentoBoleto boleto
                    && boleto.getStatus() == StatusPagamento.PENDENTE) {
                return boleto;
            }
        }
        return null;
    }
}
