package br.com.hoteliara.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

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

    private static void mostrar(AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("Hotel Iara");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
