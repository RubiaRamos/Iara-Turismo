package br.com.hoteliara;

import java.io.IOException;
import java.io.UncheckedIOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Troca de telas. Cada tela é um arquivo FXML em /fxml; o Navegador carrega
 * o arquivo e coloca o resultado como raiz da mesma Scene, assim o CSS e o
 * tamanho da janela são mantidos entre as telas.
 */
public final class Navegador {

    private static final String PASTA_FXML = "/br/com/hoteliara/fxml/";
    private static final String CSS = "/br/com/hoteliara/css/estilo.css";

    private static Stage palco;

    private Navegador() {
    }

    /** Cria a Scene inicial e mostra a janela. */
    public static void iniciar(Stage janela, String tela) {
        palco = janela;
        Scene cena = new Scene(carregar(tela), 1280, 800);
        cena.getStylesheets().add(Navegador.class.getResource(CSS).toExternalForm());
        palco.setScene(cena);
        palco.show();
    }

    /** Troca a tela atual. Exemplo: Navegador.irPara("cadastro"). */
    public static void irPara(String tela) {
        palco.getScene().setRoot(carregar(tela));
    }

    private static Parent carregar(String tela) {
        FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(PASTA_FXML + tela + ".fxml"));
        try {
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível carregar a tela: " + tela, e);
        }
    }
}
