package br.com.hoteliara.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/** Atalhos para caixas de mensagem. */
public final class Dialogos {

    private Dialogos() {
    }

    public static void aviso(String mensagem) {
        mostrar(AlertType.WARNING, "Atenção", mensagem);
    }

    public static void informacao(String titulo, String mensagem) {
        mostrar(AlertType.INFORMATION, titulo, mensagem);
    }

    public static void erro(String titulo, String mensagem) {
        mostrar(AlertType.ERROR, titulo, mensagem);
    }

    /** Pergunta sim/não. Devolve true se o usuário confirmar. */
    public static boolean confirmar(String titulo, String mensagem) {
        Alert alerta = new Alert(AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Hotel Iara");
        alerta.setHeaderText(titulo);
        ajustarTamanho(alerta);
        return alerta.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private static void mostrar(AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Hotel Iara");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        ajustarTamanho(alerta);
        alerta.showAndWait();
    }

    /** Deixa a caixa crescer para o texto aparecer inteiro, sem "...". */
    private static void ajustarTamanho(Alert alerta) {
        alerta.getDialogPane().setMinWidth(480);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }
}
