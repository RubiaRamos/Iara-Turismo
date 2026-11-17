package com.example.iara;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class HelloController {
    @FXML private VBox menuLateral;
    @FXML private TextField campoDestino;
    @FXML private DatePicker checkIn, checkOut;
    @FXML private Spinner<Integer> hospedes;

    @FXML public void initialize() {
        hospedes.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 1));
    }

    @FXML private void alternarMenu() {
        System.out.println("menu clicado!");
        boolean abrir = !menuLateral.isVisible();
        menuLateral.setVisible(abrir);
        menuLateral.setManaged(abrir);
    }

    @FXML private void irInicio() {
        menuLateral.setVisible(false);
        menuLateral.setManaged(false);
    }

    @FXML private void buscar() {
        // Integre aqui sua lógica de busca e filtragem dos hotéis.
        System.out.println("Destino: " + campoDestino.getText());
    }

    @FXML private void verDetalhes(javafx.event.ActionEvent event) {
        Button botao = (Button) event.getSource();
        System.out.println("Abrir tela de detalhes: " + botao.getUserData());
        // Substitua por sua navegação para a tela de detalhes existente.
    }
}