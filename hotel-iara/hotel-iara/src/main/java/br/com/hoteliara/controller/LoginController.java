package br.com.hoteliara.controller;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.modelo.RepositorioUsuarios;
import br.com.hoteliara.modelo.Sessao;
import br.com.hoteliara.util.Dialogos;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Tela de login (login.fxml). */
public class LoginController {

    // Os nomes abaixo precisam ser iguais aos fx:id do FXML.
    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    @FXML
    private void onEntrar() {
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            Dialogos.aviso("Preencha o e-mail e a senha.");
            return;
        }
        if (!RepositorioUsuarios.getInstancia().autenticar(email, senha)) {
            Dialogos.aviso("E-mail ou senha incorretos.");
            campoSenha.clear();
            return;
        }

        Sessao.setEmailLogado(email);
        Navegador.irPara("quartos");
    }

    @FXML
    private void onCadastrar() {
        Navegador.irPara("cadastro");
    }
}
