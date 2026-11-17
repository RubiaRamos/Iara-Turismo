package br.com.hoteliara.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.model.Cliente;
import br.com.hoteliara.model.Reserva;
import br.com.hoteliara.model.StatusReserva;
import br.com.hoteliara.service.AuthService;
import br.com.hoteliara.service.ReservaService;
import br.com.hoteliara.util.Dialogos;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;

/** "Minhas reservas" (reservas.fxml): histórico do cliente com pagar, cancelar e avaliar. */
public class ReservasController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblTitulo;
    @FXML private Label lblResumo;
    @FXML private TableView<Reserva> tabela;
    @FXML private TableColumn<Reserva, String> colNumero;
    @FXML private TableColumn<Reserva, String> colHotel;
    @FXML private TableColumn<Reserva, String> colQuarto;
    @FXML private TableColumn<Reserva, String> colPeriodo;
    @FXML private TableColumn<Reserva, String> colStatus;
    @FXML private TableColumn<Reserva, String> colTotal;
    @FXML private TableColumn<Reserva, String> colPago;
    @FXML private Button btnPagar;
    @FXML private Button btnCancelar;
    @FXML private Button btnAvaliar;

    private final AuthService authService = new AuthService();
    private final ReservaService reservaService = new ReservaService();

    @FXML
    private void initialize() {
        coluna(colNumero, r -> String.valueOf(r.getId()));
        coluna(colHotel, r -> r.getHotel().getNome());
        coluna(colQuarto, r -> r.getQuarto().getNumero() + " — " + r.getQuarto().getCategoria().getNome());
        coluna(colPeriodo, r -> r.getCheckIn().format(DATA) + " a " + r.getCheckOut().format(DATA));
        coluna(colStatus, r -> r.getStatus().name());
        coluna(colTotal, r -> Moeda.formatar(r.getValorTotal()));
        coluna(colPago, r -> Moeda.formatar(r.getValorPago()));
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, a, r) -> atualizarBotoes(r));
        carregar();
    }

    private void coluna(TableColumn<Reserva, String> coluna, Function<Reserva, String> valor) {
        coluna.setCellValueFactory(c -> new ReadOnlyStringWrapper(valor.apply(c.getValue())));
    }

    /** Lê o cliente de novo do banco: o histórico mostra sempre o estado atual. */
    private void carregar() {
        Cliente logado = Sessao.getCliente();
        if (logado == null) {
            lblTitulo.setText("Minhas reservas (entre como cliente)");
            atualizarBotoes(null);
            return;
        }
        try {
            Cliente cliente = authService.recarregarCliente(logado.getId());
            List<Reserva> historico = cliente.consultarHistorico();
            tabela.setItems(FXCollections.observableArrayList(historico));
            long ativas = historico.stream().filter(Reserva::estaAtiva).count();
            lblResumo.setText(historico.size() + " reserva(s), " + ativas + " ativa(s)");
            Sessao.entrar(cliente);
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        atualizarBotoes(null);
    }

    private void atualizarBotoes(Reserva r) {
        btnPagar.setDisable(r == null || !r.estaAtiva() || r.estaQuitada());
        btnCancelar.setDisable(r == null || !r.estaAtiva());
        btnAvaliar.setDisable(r == null || r.getStatus() != StatusReserva.CONCLUIDA);
    }

    private Reserva selecionada() {
        return tabela.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void onPagar() {
        Sessao.setReservaSelecionada(selecionada());
        Navegador.irPara("pagamento");
    }

    @FXML
    private void onCancelar() {
        Reserva r = selecionada();
        if (!Dialogos.confirmar("Cancelar a reserva nº " + r.getId() + "?",
                "O quarto será liberado e os pagamentos aprovados serão estornados.")) return;
        try {
            reservaService.cancelar(r);
            Dialogos.informacao("Reserva cancelada", "Pagamentos aprovados foram estornados.");
        } catch (IllegalStateException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        carregar();
    }

    @FXML
    private void onAvaliar() {
        Reserva r = selecionada();
        ChoiceDialog<Integer> escolha = new ChoiceDialog<>(5, 1, 2, 3, 4, 5);
        escolha.setTitle("Hotel Iara");
        escolha.setHeaderText("Que nota (1 a 5) você dá para o " + r.getHotel().getNome() + "?");
        Optional<Integer> nota = escolha.showAndWait();
        if (nota.isEmpty()) return;
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Hotel Iara");
        dialogo.setHeaderText("Comentário sobre o " + r.getHotel().getNome() + " (opcional)");
        String comentario = dialogo.showAndWait().orElse("");
        try {
            reservaService.avaliar(Sessao.getCliente(), r, nota.get(), comentario);
            Dialogos.informacao("Obrigado!", "Avaliação registrada. Nova nota média do hotel: "
                    + String.format("%.1f", r.getHotel().calcularNotaMedia()));
        } catch (IllegalStateException | IllegalArgumentException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
        carregar();
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("pesquisa");
    }
}
