package br.com.hoteliara.controller;

import br.com.hoteliara.model.TipoQuarto;

/**
 * Textos e fotos usados nas telas para cada tipo de quarto.
 * Fica junto dos controllers porque é só apresentação: a regra de preço
 * de cada tipo continua nas subclasses de Quarto (calcularDiaria).
 */
final class ApresentacaoQuarto {

    private static final String IMG = "/br/com/hoteliara/img/";

    private ApresentacaoQuarto() {
    }

    static String foto(TipoQuarto tipo) {
        return IMG + switch (tipo) {
            case PADRAO -> "quarto1.png";
            case EXECUTIVO -> "quarto2.png";
            case LUXO -> "quarto3.png";
            case SUITE -> "quarto4.png";
        };
    }

    static String descricao(TipoQuarto tipo) {
        return switch (tipo) {
            case PADRAO -> "Quarto confortável e prático, com ar-condicionado, TV de tela plana, "
                    + "frigobar e banheiro privativo.";
            case EXECUTIVO -> "Pensado para viagens a trabalho: mesa ampla, Wi-Fi rápido e "
                    + "10% de desconto em estadias a partir de 7 noites.";
            case LUXO -> "Acabamento superior, varanda e serviço de quarto. "
                    + "Inclui 10% de taxa de serviço na diária.";
            case SUITE -> "Suíte espaçosa com sala de estar e café da manhã incluso "
                    + "(R$ 80 por noite já somados no valor).";
        };
    }

    static String comodidades(TipoQuarto tipo) {
        return switch (tipo) {
            case PADRAO -> "Ar-condicionado  •  TV  •  Frigobar";
            case EXECUTIVO -> "Mesa de trabalho  •  Wi-Fi  •  TV";
            case LUXO -> "Varanda  •  Serviço de quarto  •  Wi-Fi";
            case SUITE -> "Café da manhã  •  Sala de estar  •  Hidromassagem";
        };
    }
}
