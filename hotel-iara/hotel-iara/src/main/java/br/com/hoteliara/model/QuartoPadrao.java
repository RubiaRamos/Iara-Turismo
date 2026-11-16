package br.com.hoteliara.model;

/** Quarto padrão: valor base x noites, sem acréscimo nem desconto. */
public class QuartoPadrao extends Quarto {

    public QuartoPadrao(int id, int numero, int andar, Categoria categoria, Hotel hotel) {
        super(id, numero, andar, categoria, hotel, TipoQuarto.PADRAO);
    }

    @Override
    public double calcularDiaria(int noites) {
        return getValorBase() * noites;
    }
}
