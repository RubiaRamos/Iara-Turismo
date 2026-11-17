package br.com.hoteliara.exception;

/**
 * Lançada quando o e-mail não existe ou a senha não confere.
 * A mensagem é a mesma nos dois casos, de propósito: assim ninguém descobre
 * quais e-mails estão cadastrados tentando fazer login.
 */
public class LoginInvalidoException extends Exception {

    public LoginInvalidoException() {
        super("E-mail ou senha incorretos.");
    }
}
