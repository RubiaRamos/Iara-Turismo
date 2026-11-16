package br.com.hoteliara.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.modelo.Quarto;
import br.com.hoteliara.modelo.Sessao;
import br.com.hoteliara.util.Dialogos;
import br.com.hoteliara.util.Imagens;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/** Tela de detalhe do quarto com calendário e botão Reservar (quarto.fxml). */
public class QuartoController {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private ImageView logoPequeno;
    @FXML
    private ImageView fotoQuarto;
    @FXML
    private ImageView tituloImagem;
    @FXML
    private Label lblNome;
    @FXML
    private Label lblDescricao;
    @FXML
    private Label lblDestaque;
    @FXML
    private Label lblComodidades;
    @FXML
    private Label lblData;
    @FXML
    private StackPane calendarioBox;

    // O DatePicker guarda a data escolhida; só mostramos o calendário dele.
    private final DatePicker seletor = new DatePicker();
    private Quarto quarto;

    @FXML
    private void initialize() {
        quarto = Sessao.getQuartoSelecionado();

        preencherTextos();
        montarCalendario();

        Imagens.arredondar(fotoQuarto, 28);
        Tooltip.install(logoPequeno, new Tooltip("Ver todos os quartos"));
    }

    private void preencherTextos() {
        fotoQuarto.setImage(Imagens.carregar(quarto.imagem()));
        lblDescricao.setText(quarto.descricao());
        lblDestaque.setText(quarto.destaque());
        lblComodidades.setText(quarto.comodidades());

        // Se o quarto tem título em letra cursiva (imagem), usa a imagem;
        // senão mostra o nome em texto.
        boolean temImagem = quarto.tituloImagem() != null;
        tituloImagem.setVisible(temImagem);
        tituloImagem.setManaged(temImagem);
        lblNome.setVisible(!temImagem);
        lblNome.setManaged(!temImagem);
        if (temImagem) {
            tituloImagem.setImage(Imagens.carregar(quarto.tituloImagem()));
        } else {
            lblNome.setText(quarto.nome());
        }
    }

    private void montarCalendario() {
        // Não permite escolher datas que já passaram.
        seletor.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazio) {
                super.updateItem(data, vazio);
                setDisable(vazio || data.isBefore(LocalDate.now()));
            }
        });

        // O "skin" do DatePicker sabe desenhar o calendário; pegamos só o
        // conteúdo do popup e colocamos direto na tela (calendário fixo).
        Node calendario = new DatePickerSkin(seletor).getPopupContent();
        calendarioBox.getChildren().add(calendario);

        seletor.valueProperty().addListener((obs, antiga, nova) ->
                lblData.setText(nova == null
                        ? "Selecione a data"
                        : "Data escolhida: " + nova.format(FORMATO)));
    }

    @FXML
    private void onReservar() {
        LocalDate data = seletor.getValue();
        if (data == null) {
            Dialogos.aviso("Selecione uma data antes de reservar.");
            return;
        }
        Dialogos.informacao("Reserva registrada",
                quarto.nome() + " reservado para " + data.format(FORMATO) + ".");
        Navegador.irPara("quartos");
    }

    @FXML
    private void onVerQuartos() {
        Navegador.irPara("quartos");
    }
}
