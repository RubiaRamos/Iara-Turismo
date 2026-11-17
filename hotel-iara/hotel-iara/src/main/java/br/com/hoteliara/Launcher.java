package br.com.hoteliara;

/**
 * Atalho para rodar pelo botão "Run" do IntelliJ.
 * Rodar direto uma classe que herda de Application (Main) sem módulos dá o erro
 * "JavaFX runtime components are missing"; chamando por esta classe, funciona.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}
