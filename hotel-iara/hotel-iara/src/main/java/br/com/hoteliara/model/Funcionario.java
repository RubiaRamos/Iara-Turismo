package br.com.hoteliara.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Funcionario extends Pessoa {
    private String matricula;
    private String cargo;
    private double salario;

    public Funcionario(int id, String nome, String cpf, String email,
                       String senhaHash, String telefone, LocalDate dataNascimento,
                       String matricula, String cargo, double salario) {
        super(id, nome, cpf, email, senhaHash, telefone, dataNascimento);
        setMatricula(matricula);
        setCargo(cargo);
        setSalario(salario);
    }

    @Override
    public String exibirDados() {
        return "Funcionário: " + getNome() + " | Matrícula: " + matricula
                + " | Cargo: " + cargo;
    }

    /** O funcionário cria o hotel; quem guarda (service/DAO) salva depois. */
    public Hotel cadastrarHotel(String nome, int classificacao, String telefone, Endereco endereco) {
        return new Hotel(0, nome, classificacao, telefone, endereco);
    }

    /** Cria o quarto do tipo certo (pela categoria) e já coloca no hotel. */
    public Quarto cadastrarQuarto(Hotel hotel, int numero, int andar, Categoria categoria) {
        Quarto quarto = Quarto.criar(0, numero, andar, categoria, hotel);
        hotel.adicionarQuarto(quarto);
        return quarto;
    }

    /** Todas as reservas ativas do hotel, da mais próxima para a mais distante. */
    public List<Reserva> gerenciarReservas(Hotel hotel) {
        List<Reserva> todas = new ArrayList<>();
        for (Quarto q : hotel.getQuartos()) {
            todas.addAll(q.getReservas());
        }
        todas.sort(Comparator.comparing(Reserva::getCheckIn));
        return todas;
    }

    public boolean cancelarReserva(Reserva reserva) {
        if (!reserva.estaAtiva()) return false;
        reserva.cancelar();
        return true;
    }

    public void setMatricula(String matricula) {
        if (matricula == null || matricula.isBlank())
            throw new IllegalArgumentException("Matrícula é obrigatória");
        this.matricula = matricula.trim();
    }

    public void setCargo(String cargo) {
        if (cargo == null || cargo.isBlank())
            throw new IllegalArgumentException("Cargo é obrigatório");
        this.cargo = cargo.trim();
    }

    public void setSalario(double salario) {
        if (salario < 0)
            throw new IllegalArgumentException("Salário não pode ser negativo");
        this.salario = salario;
    }

    public String getMatricula() { return matricula; }
    public String getCargo() { return cargo; }
    public double getSalario() { return salario; }
}
