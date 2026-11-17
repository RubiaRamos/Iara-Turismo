package br.com.hoteliara.controller;

import java.time.LocalDate;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.service.AuthService;
import br.com.hoteliara.util.Dialogos;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/** Tela de cadastro de cliente (cadastro.fxml). */
public class CadastroController {

    @FXML
    private ImageView logo;
    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoCpf;
    @FXML
    private TextField campoTelefone;
    @FXML
    private DatePicker campoNascimento;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenha;

    private final AuthService authService = new AuthService();

    /** Método chamado automaticamente depois que o FXML é carregado. */
    @FXML
    private void initialize() {
        Tooltip.install(logo, new Tooltip("Voltar para o login"));
        campoNascimento.setValue(LocalDate.of(2000, 1, 1));
    }

    @FXML
    private void onCriar() {
        try {
            // As validações (CPF com 11 dígitos, e-mail, senha com 6+ caracteres...)
            // estão nos setters de Pessoa e no SenhaUtil: a tela só mostra a mensagem.
            authService.cadastrarCliente(campoNome.getText(), campoCpf.getText(),
                    campoEmail.getText(), campoSenha.getText(), campoTelefone.getText(),
                    campoNascimento.getValue());
            Dialogos.informacao("Cadastro realizado", "Agora é só fazer o login.");
            Navegador.irPara("login");
        } catch (IllegalArgumentException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("login");
    }
}
