package br.com.hoteliara.controller;

import java.time.LocalDate;
import java.util.List;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.model.Hotel;
import br.com.hoteliara.model.Quarto;
import br.com.hoteliara.service.HotelService;
import br.com.hoteliara.util.Dialogos;
import br.com.hoteliara.util.Imagens;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Tela de pesquisa (pesquisa.fxml): busca quartos livres no banco e mostra em cards. */
public class PesquisaController {

    @FXML private VBox menuLateral;
    @FXML private Label lblUsuario;
    @FXML private TextField campoDestino;
    @FXML private DatePicker checkIn;
    @FXML private DatePicker checkOut;
    @FXML private Spinner<Integer> hospedes;
    @FXML private Label lblContador;
    @FXML private FlowPane resultados;

    private final HotelService hotelService = new HotelService();

    @FXML
    public void initialize() {
        hospedes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10,
                Sessao.getHospedes()));
        if (Sessao.getUsuario() != null) {
            lblUsuario.setText("Olá, " + Sessao.getUsuario().getNome().split(" ")[0]);
        }
        // Se já pesquisou antes, mantém as datas; senão sugere amanhã por 2 noites.
        LocalDate entrada = Sessao.getCheckIn() != null ? Sessao.getCheckIn() : LocalDate.now().plusDays(1);
        LocalDate saida = Sessao.getCheckOut() != null ? Sessao.getCheckOut() : entrada.plusDays(2);
        checkIn.setValue(entrada);
        checkOut.setValue(saida);
        buscar();
    }

    @FXML
    private void buscar() {
        try {
            int numHospedes = hospedes.getValue();
            List<Quarto> quartos = hotelService.buscarQuartos(campoDestino.getText(),
                    checkIn.getValue(), checkOut.getValue(), numHospedes);
            Sessao.guardarPesquisa(checkIn.getValue(), checkOut.getValue(), numHospedes);
            mostrar(quartos);
        } catch (IllegalArgumentException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    private void mostrar(List<Quarto> quartos) {
        resultados.getChildren().clear();
        lblContador.setText(quartos.size() + (quartos.size() == 1 ? " opção" : " opções"));
        if (quartos.isEmpty()) {
            Label vazio = new Label("Nenhum quarto livre para esse destino e período. Tente outras datas.");
            vazio.getStyleClass().add("vazio");
            resultados.getChildren().add(vazio);
            return;
        }
        int noites = (int) (checkOut.getValue().toEpochDay() - checkIn.getValue().toEpochDay());
        for (Quarto quarto : quartos) {
            resultados.getChildren().add(criarCard(quarto, noites));
        }
    }

    /** Monta um card no mesmo formato que a Fabrine desenhou no FXML original. */
    private VBox criarCard(Quarto quarto, int noites) {
        Hotel hotel = quarto.getHotel();

        ImageView foto = new ImageView(Imagens.carregar(ApresentacaoQuarto.foto(quarto.getTipo())));
        foto.setFitWidth(350);
        foto.setFitHeight(185);
        foto.setPreserveRatio(false);
        foto.setSmooth(true);

        Label local = estilo(new Label(hotel.getEndereco().getCidade() + " – "
                + hotel.getEndereco().getEstado()), "location");
        Label titulo = estilo(new Label(quarto.getCategoria().getNome()), "room-title");
        Label hotelInfo = estilo(new Label(hotel.getNome() + "  •  " + hotel.getClassificacao()
                + "★  •  nota " + String.format("%.1f", hotel.calcularNotaMedia())
                + "  •  quarto " + quarto.getNumero()), "hotel-info");
        hotelInfo.setWrapText(true);
        Label descricao = estilo(new Label(ApresentacaoQuarto.descricao(quarto.getTipo())), "description");
        descricao.setWrapText(true);
        descricao.setMinHeight(52);
        Label comodidades = estilo(new Label(ApresentacaoQuarto.comodidades(quarto.getTipo())
                + "  •  até " + quarto.getCapacidade() + " pessoa(s)"), "amenities");
        comodidades.setWrapText(true);

        // Polimorfismo na tela: cada tipo de quarto calcula o total do seu jeito.
        VBox precos = new VBox(3,
                estilo(new Label(noites + (noites == 1 ? " noite" : " noites") + " por"), "small"),
                estilo(new Label(Moeda.formatar(quarto.calcularDiaria(noites))), "price"),
                estilo(new Label("diária " + Moeda.formatar(quarto.getCategoria().getValorDiaria())), "small"));

        Button detalhes = estilo(new Button("Detalhes  ❯"), "details");
        detalhes.setOnAction(e -> {
            Sessao.setQuartoSelecionado(quarto);
            Navegador.irPara("quarto");
        });

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox rodape = new HBox(8, precos, espaco, detalhes);
        rodape.setAlignment(Pos.CENTER_LEFT);

        Region preenchimento = new Region();
        VBox.setVgrow(preenchimento, Priority.ALWAYS);

        VBox card = new VBox(10, foto, local, titulo, hotelInfo, descricao, comodidades,
                preenchimento, rodape);
        card.getStyleClass().add("room-card");
        card.setPadding(new Insets(15));
        return card;
    }

    private static <T extends javafx.scene.Node> T estilo(T no, String classe) {
        no.getStyleClass().add(classe);
        return no;
    }

    // ---------- menu lateral ----------

    @FXML
    private void alternarMenu() {
        boolean abrir = !menuLateral.isVisible();
        menuLateral.setVisible(abrir);
        menuLateral.setManaged(abrir);
    }

    @FXML
    private void irInicio() {
        menuLateral.setVisible(false);
        menuLateral.setManaged(false);
    }

    @FXML
    private void irReservas() {
        Navegador.irPara(Sessao.getCliente() != null ? "reservas" : "admin");
    }

    @FXML
    private void irMapa() {
        Navegador.irPara("mapa");
    }

    @FXML
    private void sair() {
        Sessao.sair();
        Navegador.irPara("login");
    }
}
