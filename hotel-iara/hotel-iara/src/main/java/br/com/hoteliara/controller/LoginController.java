package br.com.hoteliara.controller;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.exception.LoginInvalidoException;
import br.com.hoteliara.model.Pessoa;
import br.com.hoteliara.service.AuthService;
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

    private final AuthService authService = new AuthService();

    @FXML
    private void onEntrar() {
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        if (email.isEmpty() || senha.isEmpty()) {
            Dialogos.aviso("Preencha o e-mail e a senha.");
            return;
        }
        try {
            Pessoa pessoa = authService.entrar(email, senha);
            Sessao.entrar(pessoa);
            Navegador.irPara("pesquisa");
        } catch (LoginInvalidoException e) {
            Dialogos.aviso(e.getMessage());
            campoSenha.clear();
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    @FXML
    private void onCadastrar() {
        Navegador.irPara("cadastro");
    }
}
