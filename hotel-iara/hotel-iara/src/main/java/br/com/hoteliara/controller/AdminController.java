package br.com.hoteliara.controller;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.exception.PagamentoRecusadoException;
import br.com.hoteliara.model.Funcionario;
import br.com.hoteliara.model.Hotel;
import br.com.hoteliara.model.Reserva;
import br.com.hoteliara.model.StatusReserva;
import br.com.hoteliara.service.HotelService;
import br.com.hoteliara.service.ReservaService;
import br.com.hoteliara.util.Dialogos;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Área do funcionário (admin.fxml): reservas ativas de todos os hotéis,
 * com confirmar, concluir (check-out), cancelar e dar baixa no boleto.
 */
public class AdminController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblTitulo;
    @FXML private Label lblResumo;
    @FXML private TableView<Reserva> tabela;
    @FXML private TableColumn<Reserva, String> colNumero;
    @FXML private TableColumn<Reserva, String> colCliente;
    @FXML private TableColumn<Reserva, String> colHotel;
    @FXML private TableColumn<Reserva, String> colQuarto;
    @FXML private TableColumn<Reserva, String> colPeriodo;
    @FXML private TableColumn<Reserva, String> colStatus;
    @FXML private TableColumn<Reserva, String> colTotal;
    @FXML private TableColumn<Reserva, String> colPago;
    @FXML private TableColumn<Reserva, String> colBoleto;
    @FXML private Button btnConfirmar;
    @FXML private Button btnConcluir;
    @FXML private Button btnCancelar;
    @FXML private Button btnBaixa;

    private final HotelService hotelService = new HotelService();
    private final ReservaService reservaService = new ReservaService();
    private Funcionario funcionario;

    @FXML
    private void initialize() {
        if (!(Sessao.getUsuario() instanceof Funcionario f)) {
            Dialogos.aviso("Esta área é só para funcionários.");
            Navegador.irPara("login");
            return;
        }
        funcionario = f;
        lblTitulo.setText("Área do funcionário — " + f.getNome() + " (" + f.getCargo() + ")");

        coluna(colNumero, r -> String.valueOf(r.getId()));
        coluna(colCliente, r -> r.getCliente().getNome());
        coluna(colHotel, r -> r.getHotel().getNome());
        coluna(colQuarto, r -> r.getQuarto().getNumero() + " — " + r.getQuarto().getCategoria().getNome());
        coluna(colPeriodo, r -> r.getCheckIn().format(DATA) + " a " + r.getCheckOut().format(DATA));
        coluna(colStatus, r -> r.getStatus().name());
        coluna(colTotal, r -> Moeda.formatar(r.getValorTotal()));
        coluna(colPago, r -> Moeda.formatar(r.getValorPago()));
        coluna(colBoleto, r -> {
            var boleto = reservaService.boletoPendente(r);
            return boleto == null ? "—" : "pendente " + Moeda.formatar(boleto.getValor());
        });
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, a, r) -> atualizarBotoes(r));
        carregar();
    }

    private void coluna(TableColumn<Reserva, String> coluna, Function<Reserva, String> valor) {
        coluna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
    }

    /** Lê os hotéis do banco e junta as reservas ativas de cada um. */
    private void carregar() {
        try {
            List<Reserva> reservas = new ArrayList<>();
            for (Hotel hotel : hotelService.carregarHoteis()) {
                // O Funcionario sabe juntar as reservas dos quartos de um hotel.
                reservas.addAll(funcionario.gerenciarReservas(hotel));
            }
            reservas.sort(Comparator.comparing(Reserva::getCheckIn));
            tabela.setItems(FXCollections.observableArrayList(reservas));
            long pendentes = reservas.stream().filter(r -> r.getStatus() == StatusReserva.PENDENTE).count();
            lblResumo.setText(reservas.size() + " reserva(s) ativa(s), " + pendentes + " pendente(s)");
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        atualizarBotoes(null);
    }

    private void atualizarBotoes(Reserva r) {
        btnConfirmar.setDisable(r == null || r.getStatus() != StatusReserva.PENDENTE);
        btnConcluir.setDisable(r == null || r.getStatus() != StatusReserva.CONFIRMADA);
        btnCancelar.setDisable(r == null || !r.estaAtiva());
        btnBaixa.setDisable(r == null || reservaService.boletoPendente(r) == null);
    }

    private Reserva selecionada() {
        return tabela.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onConfirmar() {
        executar(() -> {
            reservaService.confirmar(selecionada());
            Dialogos.informacao("Reserva confirmada", "A reserva agora está CONFIRMADA.");
        });
    }

    @FXML
    private void onConcluir() {
        Reserva r = selecionada();
        if (!Dialogos.confirmar("Concluir a reserva nº " + r.getId() + "?",
                "Use no check-out: o quarto é liberado e o cliente passa a poder avaliar o hotel.")) return;
        executar(() -> {
            reservaService.concluir(r);
            Dialogos.informacao("Reserva concluída", r.getCliente().getNome() + " já pode avaliar o hotel.");
        });
    }

    @FXML
    private void onCancelar() {
        Reserva r = selecionada();
        if (!Dialogos.confirmar("Cancelar a reserva nº " + r.getId() + "?",
                "O quarto será liberado e os pagamentos aprovados serão estornados.")) return;
        executar(() -> {
            reservaService.cancelar(r);
            Dialogos.informacao("Reserva cancelada", "Pagamentos aprovados foram estornados.");
        });
    }

    @FXML
    private void onBaixaBoleto() {
        Reserva r = selecionada();
        try {
            reservaService.registrarBaixaBoleto(r);
            Dialogos.informacao("Boleto compensado", r.estaQuitada()
                    ? "Reserva quitada e confirmada."
                    : "Ainda falta pagar " + Moeda.formatar(r.getSaldoDevedor()) + ".");
        } catch (PagamentoRecusadoException e) {
            Dialogos.aviso("Não foi possível dar baixa: " + e.getMessage());
        } catch (IllegalStateException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        carregar();
    }

    /** Executa uma ação, mostra o erro se a regra não permitir e recarrega a tabela. */
    private void executar(Runnable acao) {
        try {
            acao.run();
        } catch (IllegalStateException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        carregar();
    }

    @FXML
    private void onPesquisa() {
        Navegador.irPara("pesquisa");
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("admin");
    }

    @FXML
    private void onSair() {
        Sessao.sair();
        Navegador.irPara("login");
    }
}
