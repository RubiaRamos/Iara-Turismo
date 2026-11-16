package br.com.hoteliara.model;

/** Quarto luxo: valor base x noites + 10% de taxa de serviço. */
public class QuartoLuxo extends Quarto {

    private static final double TAXA_SERVICO = 0.10;

    public QuartoLuxo(int id, int numero, int andar, Categoria categoria, Hotel hotel) {
        super(id, numero, andar, categoria, hotel, TipoQuarto.LUXO);
    }

    @Override
    public double calcularDiaria(int noites) {
        return getValorBase() * noites * (1 + TAXA_SERVICO);
    }
}
