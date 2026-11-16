package br.com.hoteliara.modelo;

import java.util.List;

/**
 * Lista fixa de quartos do hotel. Em um projeto maior, estes dados viriam de
 * um banco de dados ou de uma API.
 */
public final class CatalogoQuartos {

    private static final String IMG = "/br/com/hoteliara/img/";

    private static final String DESCRICAO_GENERICA =
            "Quarto decorado em estilo contemporâneo, com ar-condicionado, TV de tela plana, "
                    + "frigobar, banheiro privativo com chuveiro, amenidades de banho de cortesia "
                    + "e secador de cabelo.";

    private static final List<Quarto> QUARTOS = List.of(
            new Quarto("quarto1", "Quarto Standard",
                    DESCRICAO_GENERICA,
                    "Ideal para uma estadia tranquila:",
                    "❄️ Ar-condicionado | 📺 TV de tela plana | 🧊 Frigobar | 🛎️ Recepção 24 horas "
                            + "| 🧹 Serviço de limpeza diário",
                    IMG + "quarto1.png", null),
            new Quarto("quarto2", "Quarto Triplo",
                    DESCRICAO_GENERICA,
                    "Ideal para viajar em grupo:",
                    "❄️ Ar-condicionado | 📺 TV de tela plana | 🧊 Frigobar | 🧳 Depósito de bagagens "
                            + "| ☕ Café da manhã muito bom",
                    IMG + "quarto2.png", null),
            new Quarto("quarto3", "Quarto Duplo",
                    DESCRICAO_GENERICA,
                    "Ideal para viajar com um amigo:",
                    "❄️ Ar-condicionado | 📺 TV de tela plana | 🧊 Frigobar | 🏊 Piscina ao ar livre "
                            + "| ☕ Café da manhã muito bom",
                    IMG + "quarto3.png", null),
            new Quarto("quarto4", "Quarto Casal",
                    "Decorados em estilo contemporâneo, todos os quartos deste hotel 4 estrelas "
                            + "incluem ar-condicionado, TV de tela plana, frigobar, um banheiro privativo "
                            + "com chuveiro, amenidades de banho de cortesia e secador de cabelo.\n\n"
                            + "O Hotel Atlântico Travel Copacabana fica a 3 km da famosa Praia de Ipanema "
                            + "e a 6 km do Aeroporto Santos Dumont. Já o Aeroporto Internacional do Galeão "
                            + "está situado a 25,5 km de distância.",
                    "Ideal para sua viagem a dois à praia:",
                    "🏖️ Cadeiras / espreguiçadeiras de praia | 🧖 Sauna | 🛎️ Serviço de quarto "
                            + "| 🥐 Café da manhã no quarto | 🧳 Depósito de bagagens "
                            + "| 🏊 Piscina ao ar livre | 🏋️ Academia | 🛎️ Recepção 24 horas "
                            + "| 🧹 Serviço de limpeza diário | ☕ Café da manhã muito bom",
                    IMG + "quarto-casal-grande.png", IMG + "titulo-quarto-casal.png"));

    private CatalogoQuartos() {
    }

    public static Quarto buscar(String id) {
        return QUARTOS.stream()
                .filter(q -> q.id().equals(id))
                .findFirst()
                .orElse(padrao());
    }

    /** Quarto exibido quando nenhum foi escolhido. */
    public static Quarto padrao() {
        return QUARTOS.get(QUARTOS.size() - 1);
    }
}
