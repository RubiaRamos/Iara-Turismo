package br.com.hoteliara.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.exception.QuartoIndisponivelException;
import br.com.hoteliara.exception.ReservaInvalidaException;
import br.com.hoteliara.model.Cliente;
import br.com.hoteliara.model.Hotel;
import br.com.hoteliara.model.Quarto;
import br.com.hoteliara.model.Reserva;
import br.com.hoteliara.service.ReservaService;
import br.com.hoteliara.util.Dialogos;
import br.com.hoteliara.util.Imagens;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/** Detalhe do quarto escolhido na pesquisa, com o formulário de reserva (quarto.fxml). */
public class QuartoController {

    @FXML private ImageView logoPequeno;
    @FXML private ImageView fotoQuarto;
    @FXML private Label lblNome;
    @FXML private Label lblHotel;
    @FXML private Label lblEndereco;
    @FXML private Label lblDescricao;
    @FXML private Label lblDestaque;
    @FXML private Label lblComodidades;
    @FXML private DatePicker campoCheckIn;
    @FXML private DatePicker campoCheckOut;
    @FXML private Spinner<Integer> campoHospedes;
    @FXML private Label lblNoites;
    @FXML private Label lblTotal;

    private final ReservaService reservaService = new ReservaService();
    private Quarto quarto;

    @FXML
    private void initialize() {
        quarto = Sessao.getQuartoSelecionado();
        preencherTextos();
        prepararFormulario();
        Imagens.arredondar(fotoQuarto, 28);
        Tooltip.install(logoPequeno, new Tooltip("Voltar para a pesquisa"));
    }

    private void preencherTextos() {
        Hotel hotel = quarto.getHotel();
        fotoQuarto.setImage(Imagens.carregar(ApresentacaoQuarto.foto(quarto.getTipo())));
        lblNome.setText(quarto.getCategoria().getNome());
        lblHotel.setText(hotel.getNome() + "  •  " + hotel.getClassificacao() + " estrelas  •  nota "
                + String.format("%.1f", hotel.calcularNotaMedia()));
        lblEndereco.setText(hotel.getEndereco().getEnderecoFormatado());
        lblDescricao.setText(ApresentacaoQuarto.descricao(quarto.getTipo()));
        lblDestaque.setText("Quarto " + quarto.getNumero() + ", " + quarto.getAndar() + "º andar, até "
                + quarto.getCapacidade() + " pessoa(s)");
        lblComodidades.setText(ApresentacaoQuarto.comodidades(quarto.getTipo())
                + "\nDiária base: " + Moeda.formatar(quarto.getCategoria().getValorDiaria()));
    }

    private void prepararFormulario() {
        campoHospedes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                1, quarto.getCapacidade(), Math.min(Sessao.getHospedes(), quarto.getCapacidade())));
        campoCheckIn.setValue(Sessao.getCheckIn());
        campoCheckOut.setValue(Sessao.getCheckOut());
        // Não deixa escolher datas que já passaram.
        campoCheckIn.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setDisable(vazio || data.isBefore(LocalDate.now()));
            }
        });
        campoCheckIn.valueProperty().addListener((obs, a, b) -> atualizarTotal());
        campoCheckOut.valueProperty().addListener((obs, a, b) -> atualizarTotal());
        atualizarTotal();
    }

    /**
     * POLIMORFISMO: o controller não sabe se o quarto é Padrão, Luxo ou Suíte.
     * Ele só chama calcularDiaria() e cada subclasse responde com a sua regra.
     */
    private void atualizarTotal() {
        LocalDate entrada = campoCheckIn.getValue();
        LocalDate saida = campoCheckOut.getValue();
        if (entrada == null || saida == null || !saida.isAfter(entrada)) {
            lblNoites.setText("Escolha as datas");
            lblTotal.setText("");
            return;
        }
        int noites = (int) ChronoUnit.DAYS.between(entrada, saida);
        lblNoites.setText(noites + (noites == 1 ? " noite" : " noites") + " — total:");
        lblTotal.setText(Moeda.formatar(quarto.calcularDiaria(noites)));
    }

    @FXML
    private void onReservar() {
        Cliente cliente = Sessao.getCliente();
        if (cliente == null) {
            Dialogos.aviso("Entre com uma conta de cliente para reservar.");
            return;
        }
        try {
            Reserva reserva = reservaService.reservar(cliente, quarto, campoCheckIn.getValue(),
                    campoCheckOut.getValue(), campoHospedes.getValue());
            Sessao.setReservaSelecionada(reserva);
            Dialogos.informacao("Reserva criada",
                    "Reserva nº " + reserva.getId() + " criada. Agora escolha a forma de pagamento.");
            Navegador.irPara("pagamento");
        } catch (ReservaInvalidaException | QuartoIndisponivelException e) {
            // As duas exceções vêm da Reserva/Quarto e têm a mensagem certa para o usuário.
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    @FXML
    private void onVerQuartos() {
        Navegador.irPara("pesquisa");
    }
}
