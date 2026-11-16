package br.com.hoteliara.util;

import java.io.InputStream;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

/** Utilidades para trabalhar com imagens. */
public final class Imagens {

    private Imagens() {
    }

    /** Carrega uma imagem que está dentro de src/main/resources. */
    public static Image carregar(String caminho) {
        InputStream entrada = Imagens.class.getResourceAsStream(caminho);
        if (entrada == null) {
            throw new IllegalArgumentException("Imagem não encontrada: " + caminho);
        }
        return new Image(entrada);
    }

    /**
     * Deixa os cantos da imagem arredondados usando um "clip".
     * O clip acompanha o tamanho do ImageView mesmo quando a imagem muda.
     */
    public static void arredondar(ImageView vista, double raio) {
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(raio * 2);
        recorte.setArcHeight(raio * 2);
        recorte.setWidth(vista.getLayoutBounds().getWidth());
        recorte.setHeight(vista.getLayoutBounds().getHeight());
        vista.layoutBoundsProperty().addListener((obs, antigo, novo) -> {
            recorte.setWidth(novo.getWidth());
            recorte.setHeight(novo.getHeight());
        });
        vista.setClip(recorte);
    }
}
