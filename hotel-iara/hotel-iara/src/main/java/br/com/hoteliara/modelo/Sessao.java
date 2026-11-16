package br.com.hoteliara.modelo;

/** Guarda o que precisa ser lembrado ao trocar de tela. */
public final class Sessao {

    private static String emailLogado;
    private static Quarto quartoSelecionado;

    private Sessao() {
    }

    public static String getEmailLogado() {
        return emailLogado;
    }

    public static void setEmailLogado(String email) {
        emailLogado = email;
    }

    public static Quarto getQuartoSelecionado() {
        return quartoSelecionado != null ? quartoSelecionado : CatalogoQuartos.padrao();
    }

    public static void setQuartoSelecionado(Quarto quarto) {
        quartoSelecionado = quarto;
    }
}
