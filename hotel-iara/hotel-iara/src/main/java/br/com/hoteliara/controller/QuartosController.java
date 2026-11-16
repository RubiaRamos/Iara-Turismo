package br.com.hoteliara.controller;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.modelo.CatalogoQuartos;
import br.com.hoteliara.modelo.Sessao;
import br.com.hoteliara.util.Imagens;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/** Tela com a lista de quartos (quartos.fxml). */
public class QuartosController {

    @FXML
    private ImageView foto1;
    @FXML
    private ImageView foto2;
    @FXML
    private ImageView foto3;
    @FXML
    private ImageView foto4;

    @FXML
    private void initialize() {
        // Cantos arredondados nas fotos dos cards.
        for (ImageView foto : new ImageView[] {foto1, foto2, foto3, foto4}) {
            Imagens.arredondar(foto, 22);
        }
    }

    /**
     * Um único método atende os 4 cards: o id do card (quarto1..quarto4)
     * diz qual quarto foi clicado.
     */
    @FXML
    private void onAbrirQuarto(MouseEvent evento) {
        Node card = (Node) evento.getSource();
        Sessao.setQuartoSelecionado(CatalogoQuartos.buscar(card.getId()));
        Navegador.irPara("quarto");
    }
}
