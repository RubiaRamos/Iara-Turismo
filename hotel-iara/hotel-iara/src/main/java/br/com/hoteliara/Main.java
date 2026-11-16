package br.com.hoteliara;

import java.util.Locale;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Ponto de entrada do aplicativo Hotel Iara.
 * Apenas configura o idioma, o palco (Stage) e abre a tela de login.
 */
public class Main extends Application {

    public static void main(String[] args) {
        // Faz o calendário mostrar meses e dias da semana em português.
        Locale.setDefault(Locale.forLanguageTag("pt-BR"));
        launch(args);
    }

    @Override
    public void start(Stage palco) {
        palco.setTitle("Hotel Iara");
        palco.getIcons().add(new Image(
                Main.class.getResourceAsStream("/br/com/hoteliara/img/logo.png")));
        palco.setMinWidth(1100);
        palco.setMinHeight(720);

        Navegador.iniciar(palco, "login");
    }
}
