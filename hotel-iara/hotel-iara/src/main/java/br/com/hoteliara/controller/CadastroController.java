package br.com.hoteliara.controller;

import java.util.regex.Pattern;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.modelo.RepositorioUsuarios;
import br.com.hoteliara.util.Dialogos;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

/** Tela de cadastro (cadastro.fxml). */
public class CadastroController {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    @FXML
    private ImageView logo;

    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoSenha;

    /** Método chamado automaticamente depois que o FXML é carregado. */
    @FXML
    private void initialize() {
        Tooltip.install(logo, new Tooltip("Voltar para o login"));
    }

    @FXML
    private void onCriar() {
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText();

        if (!EMAIL.matcher(email).matches()) {
            Dialogos.aviso("Digite um e-mail válido.");
            return;
        }
        if (senha.length() < 6) {
            Dialogos.aviso("A senha precisa ter pelo menos 6 caracteres.");
            return;
        }
        if (!RepositorioUsuarios.getInstancia().cadastrar(email, senha)) {
            Dialogos.aviso("Já existe uma conta com este e-mail.");
            return;
        }

        Dialogos.informacao("Cadastro realizado", "Agora é só fazer o login.");
        Navegador.irPara("login");
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("login");
    }
}
