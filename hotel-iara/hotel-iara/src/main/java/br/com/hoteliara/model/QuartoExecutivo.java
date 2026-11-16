package br.com.hoteliara.model;

/** Quarto executivo: 10% de desconto a partir de 7 noites (estadias longas a trabalho). */
public class QuartoExecutivo extends Quarto {

    private static final int NOITES_PARA_DESCONTO = 7;
    private static final double DESCONTO = 0.10;

    public QuartoExecutivo(int id, int numero, int andar, Categoria categoria, Hotel hotel) {
        super(id, numero, andar, categoria, hotel, TipoQuarto.EXECUTIVO);
    }

    @Override
    public double calcularDiaria(int noites) {
        double total = getValorBase() * noites;
        if (noites >= NOITES_PARA_DESCONTO) {
            total = total * (1 - DESCONTO);
        }
        return total;
    }
}
