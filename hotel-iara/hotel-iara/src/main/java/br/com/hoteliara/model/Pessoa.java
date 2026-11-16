package br.com.hoteliara.model;

import java.time.LocalDate;

import br.com.hoteliara.util.SenhaUtil;

public abstract class Pessoa {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String senhaHash;
    private String telefone;
    private LocalDate dataNascimento;

    /**
     * @param senhaHash a senha JÁ transformada em hash. Para um cadastro novo,
     *                  use SenhaUtil.gerarHash(senhaDigitada); o DAO passa o
     *                  hash que veio do banco.
     */
    public Pessoa(int id, String nome, String cpf, String email,
                  String senhaHash, String telefone, LocalDate dataNascimento) {
        this.id = id;
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        if (senhaHash == null || senhaHash.isBlank())
            throw new IllegalArgumentException("Senha é obrigatória");
        this.senhaHash = senhaHash;
        setTelefone(telefone);
        setDataNascimento(dataNascimento);
    }

    public abstract String exibirDados();

    /** Gera o hash da senha digitada e compara com o guardado. */
    public boolean autenticar(String senha) {
        if (senha == null || senha.isEmpty()) return false;
        try {
            return senhaHash.equals(SenhaUtil.gerarHash(senha));
        } catch (IllegalArgumentException e) {
            return false; // senha curta demais nunca é a certa
        }
    }

    /** Troca a senha: exige a atual, para ninguém trocar a senha de outra pessoa. */
    public void alterarSenha(String senhaAtual, String novaSenha) {
        if (!autenticar(senhaAtual))
            throw new IllegalArgumentException("Senha atual incorreta");
        this.senhaHash = SenhaUtil.gerarHash(novaSenha);
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome não pode ser vazio");
        this.nome = nome.trim();
    }

    /** Aceita "123.456.789-09" ou "12345678909" e guarda só os 11 dígitos. */
    public void setCpf(String cpf) {
        String digitos = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (digitos.length() != 11)
            throw new IllegalArgumentException("CPF deve ter 11 dígitos");
        this.cpf = digitos;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email inválido");
        this.email = email.trim().toLowerCase();
    }

    /** Aceita "(41) 99999-8888" ou "41999998888" e guarda só os dígitos. */
    public void setTelefone(String telefone) {
        String digitos = telefone == null ? "" : telefone.replaceAll("\\D", "");
        if (!digitos.matches("\\d{10,11}"))
            throw new IllegalArgumentException("Telefone deve ter 10 ou 11 dígitos");
        this.telefone = digitos;
    }

    public void setDataNascimento(LocalDate data) {
        if (data == null || data.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de nascimento inválida");
        this.dataNascimento = data;
    }

    /** O DAO usa para colocar o id gerado pelo banco. */
    public void setId(int id) { this.id = id; }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getTelefone() { return telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }

    /** Só o hash sai daqui (o DAO precisa para salvar); a senha real nunca é guardada. */
    public String getSenhaHash() { return senhaHash; }
}
