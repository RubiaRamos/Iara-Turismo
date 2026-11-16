package br.com.hoteliara.model;

/**
 * Categoria de quarto: guarda o que vários quartos têm em comum (nome,
 * capacidade e preço da diária). Ex.: todos os quartos "Luxo Casal" de um
 * hotel custam o mesmo; se o preço mudar, muda num lugar só.
 */
public class Categoria {

    private int id;
    private String nome;
    private TipoQuarto tipo;
    private int capacidade;
    private double valorDiaria;

    public Categoria(int id, String nome, TipoQuarto tipo, int capacidade, double valorDiaria) {
        this.id = id;
        setNome(nome);
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo é obrigatório.");
        }
        this.tipo = tipo;
        setCapacidade(capacidade);
        setValorDiaria(valorDiaria);
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public TipoQuarto getTipo() { return tipo; }
    public int getCapacidade() { return capacidade; }
    public double getValorDiaria() { return valorDiaria; }

    /** O DAO usa para colocar o id gerado pelo banco. */
    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public void setCapacidade(int capacidade) {
        if (capacidade < 1) {
            throw new IllegalArgumentException("Capacidade deve ser pelo menos 1.");
        }
        this.capacidade = capacidade;
    }

    public void setValorDiaria(double valorDiaria) {
        if (valorDiaria <= 0) {
            throw new IllegalArgumentException("Valor da diária deve ser maior que zero.");
        }
        this.valorDiaria = valorDiaria;
    }

    // O tipo não tem setter: ele decide qual subclasse de Quarto foi criada,
    // então trocá-lo depois deixaria o quarto incoerente.

    @Override
    public String toString() {
        return String.format("%s (%d pessoas) - R$ %.2f", nome, capacidade, valorDiaria);
    }
}
