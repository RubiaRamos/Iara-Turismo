package br.com.hoteliara.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Hotel: CONTÉM seus quartos (associação um-para-muitos) e reúne as
 * avaliações dos hóspedes.
 */
public class Hotel {

    private int id;
    private String nome;
    private int classificacao; // estrelas, de 1 a 5
    private String telefone;
    private Endereco endereco;
    private final List<Quarto> quartos = new ArrayList<>();
    private final List<Avaliacao> avaliacoes = new ArrayList<>();

    public Hotel(int id, String nome, int classificacao, String telefone, Endereco endereco) {
        this.id = id;
        setNome(nome);
        setClassificacao(classificacao);
        setTelefone(telefone);
        setEndereco(endereco);
    }

    // ---------- quartos ----------

    /** Adiciona um quarto, sem deixar repetir o número dentro do mesmo hotel. */
    public void adicionarQuarto(Quarto quarto) {
        if (quarto.getHotel() != this) {
            throw new IllegalArgumentException("Esse quarto pertence a outro hotel.");
        }
        boolean numeroRepetido = quartos.stream()
                .anyMatch(q -> q.getNumero() == quarto.getNumero());
        if (numeroRepetido) {
            throw new IllegalArgumentException("Já existe o quarto " + quarto.getNumero()
                    + " neste hotel.");
        }
        quartos.add(quarto);
    }

    public void removerQuarto(Quarto quarto) {
        quartos.remove(quarto);
    }

    /** Quartos livres no período, do mais barato para o mais caro. */
    public List<Quarto> listarQuartosDisponiveis(LocalDate checkIn, LocalDate checkOut) {
        return listarQuartosDisponiveis(checkIn, checkOut, 1);
    }

    /** Mesma coisa, mas só os que comportam o número de hóspedes. */
    public List<Quarto> listarQuartosDisponiveis(LocalDate checkIn, LocalDate checkOut,
                                                 int hospedes) {
        return quartos.stream()
                .filter(q -> q.comporta(hospedes))
                .filter(q -> q.estaDisponivel(checkIn, checkOut))
                .sorted(Comparator.comparingDouble(q -> q.getCategoria().getValorDiaria()))
                .toList();
    }

    /** Menor diária do hotel (para filtrar e ordenar a pesquisa por preço). */
    public double getMenorDiaria() {
        return quartos.stream()
                .mapToDouble(q -> q.getCategoria().getValorDiaria())
                .min()
                .orElse(0);
    }

    // ---------- avaliações ----------

    public void adicionarAvaliacao(Avaliacao avaliacao) {
        avaliacoes.add(avaliacao);
    }

    /** Média das notas (1 a 5). Sem avaliações, devolve 0. */
    public double calcularNotaMedia() {
        return avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0);
    }

    // ---------- getters e setters ----------

    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getClassificacao() { return classificacao; }
    public String getTelefone() { return telefone; }
    public Endereco getEndereco() { return endereco; }

    public List<Quarto> getQuartos() {
        return Collections.unmodifiableList(quartos);
    }

    public List<Avaliacao> getAvaliacoes() {
        return Collections.unmodifiableList(avaliacoes);
    }

    /** O DAO usa para colocar o id gerado pelo banco. */
    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do hotel é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public void setClassificacao(int classificacao) {
        if (classificacao < 1 || classificacao > 5) {
            throw new IllegalArgumentException("Classificação deve ser de 1 a 5 estrelas.");
        }
        this.classificacao = classificacao;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new IllegalArgumentException("Endereço é obrigatório.");
        }
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return nome + " (" + classificacao + "★) - " + endereco.getCidade();
    }
}
