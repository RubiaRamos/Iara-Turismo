package br.com.hoteliara.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import br.com.hoteliara.model.*;

public class PessoaDAO implements Dao<Pessoa> {
    private String tipo(Pessoa pessoa) {
        if (pessoa instanceof Cliente) return "CLIENTE";
        if (pessoa instanceof Funcionario) return "FUNCIONARIO";
        throw new IllegalArgumentException("Tipo de pessoa nao suportado.");
    }

    private Object[] valores(Pessoa pessoa) {
        return new Object[] {tipo(pessoa), pessoa.getNome(), pessoa.getCpf(), pessoa.getEmail(),
                pessoa.getSenhaHash(), pessoa.getTelefone(), pessoa.getDataNascimento()};
    }

    @Override
    public void salvar(Pessoa pessoa) {
        if (pessoa == null) throw new IllegalArgumentException("Pessoa e obrigatoria.");
        if (pessoa.getId() != 0) throw new IllegalArgumentException("Pessoa ja possui ID; use atualizar.");
        try (Connection c = ConexaoBD.conectar()) {
            // Pessoa e funcionario devem ser gravados juntos ou nenhum dos dois.
            c.setAutoCommit(false);
            try {
                int id = Jdbc.inserir(c, "INSERT INTO pessoa "
                        + "(tipo, nome, cpf, email, senha_hash, telefone, dt_nasc) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        valores(pessoa));
                if (pessoa instanceof Funcionario f)
                    Jdbc.executar(c, "INSERT INTO funcionario (id_pessoa, matricula, cargo, salario) "
                            + "VALUES (?, ?, ?, ?)", id, f.getMatricula(), f.getCargo(), f.getSalario());
                c.commit();
                pessoa.setId(id);
            } catch (SQLException | RuntimeException erro) {
                Jdbc.desfazer(c, erro);
                throw erro;
            }
        } catch (SQLException erro) {
            throw Jdbc.falha("salvar a pessoa", erro);
        }
    }

    @Override
    public Pessoa buscarPorId(int id) {
        return DadosBD.carregar().pessoas.get(id);
    }

    @Override
    public List<Pessoa> listar() {
        return new ArrayList<>(DadosBD.carregar().pessoas.values());
    }

    @Override
    public void atualizar(Pessoa pessoa) {
        if (pessoa == null) throw new IllegalArgumentException("Pessoa e obrigatoria.");
        Jdbc.id(pessoa.getId());
        try (Connection c = ConexaoBD.conectar()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT tipo FROM pessoa WHERE id_pessoa = ? FOR UPDATE")) {
                    ps.setInt(1, pessoa.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Pessoa nao encontrada.");
                        if (!tipo(pessoa).equals(rs.getString("tipo")))
                            throw new IllegalArgumentException("Nao e permitido trocar o tipo da pessoa.");
                    }
                }
                Jdbc.executar(c, "UPDATE pessoa SET nome = ?, cpf = ?, email = ?, senha_hash = ?, "
                        + "telefone = ?, dt_nasc = ? WHERE id_pessoa = ?",
                        pessoa.getNome(), pessoa.getCpf(), pessoa.getEmail(), pessoa.getSenhaHash(),
                        pessoa.getTelefone(), pessoa.getDataNascimento(), pessoa.getId());
                if (pessoa instanceof Funcionario f)
                    Jdbc.executar(c, "UPDATE funcionario SET matricula = ?, cargo = ?, salario = ? "
                            + "WHERE id_pessoa = ?", f.getMatricula(), f.getCargo(), f.getSalario(), f.getId());
                c.commit();
            } catch (SQLException | RuntimeException erro) {
                Jdbc.desfazer(c, erro);
                throw erro;
            }
        } catch (SQLException erro) {
            throw Jdbc.falha("atualizar a pessoa", erro);
        }
    }

    @Override
    public void remover(int id) {
        // O banco remove funcionario por CASCADE e protege clientes com reservas por RESTRICT.
        Jdbc.executar("DELETE FROM pessoa WHERE id_pessoa = ?", Jdbc.id(id));
    }
}

