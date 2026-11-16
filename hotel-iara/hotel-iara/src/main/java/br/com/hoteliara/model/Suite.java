package br.com.hoteliara.model;

/** Suíte: valor base + R$ 80 de café da manhã por noite. */
public class Suite extends Quarto {

    private static final double CAFE_DA_MANHA_POR_NOITE = 80.0;

    public Suite(int id, int numero, int andar, Categoria categoria, Hotel hotel) {
        super(id, numero, andar, categoria, hotel, TipoQuarto.SUITE);
    }

    @Override
    public double calcularDiaria(int noites) {
        return (getValorBase() + CAFE_DA_MANHA_POR_NOITE) * noites;
    }
}
