package br.com.hoteliara.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import br.com.hoteliara.exception.BancoDadosException;

// Apenas operacoes JDBC repetidas; as consultas ficam nos DAOs de cada entidade.
final class Jdbc {
    private Jdbc() {}

    static int id(int id) {
        if (id <= 0) throw new IllegalArgumentException("Salve os objetos relacionados antes deste registro.");
        return id;
    }

    static void preencher(PreparedStatement ps, Object... valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            Object valor = valores[i];
            if (valor instanceof Double numero) {
                if (!Double.isFinite(numero)) throw new IllegalArgumentException("Numero nao finito.");
                // Dinheiro no banco usa DECIMAL; o contrato do model continua double.
                ps.setBigDecimal(i + 1, BigDecimal.valueOf(numero));
            } else if (valor instanceof LocalDate data) {
                ps.setDate(i + 1, Date.valueOf(data));
            } else {
                ps.setObject(i + 1, valor);
            }
        }
    }

    static int inserir(Connection conexao, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(ps, valores);
            ps.executeUpdate();
            try (ResultSet ids = ps.getGeneratedKeys()) {
                if (!ids.next()) throw new SQLException("O banco nao devolveu o identificador.");
                return ids.getInt(1);
            }
        }
    }

    static int inserir(String sql, Object... valores) {
        try (Connection conexao = ConexaoBD.conectar()) {
            return inserir(conexao, sql, valores);
        } catch (SQLException erro) {
            throw falha("salvar o registro", erro);
        }
    }

    static void executar(Connection conexao, String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            preencher(ps, valores);
            ps.executeUpdate();
        }
    }

    static void executar(String sql, Object... valores) {
        try (Connection conexao = ConexaoBD.conectar()) {
            executar(conexao, sql, valores);
        } catch (SQLException erro) {
            throw falha("alterar ou remover o registro", erro);
        }
    }

    static BancoDadosException falha(String operacao, SQLException erro) {
        String detalhe = erro.getSQLState() != null && erro.getSQLState().startsWith("23")
                ? " Confira dados duplicados, campos obrigatorios ou registros vinculados." : "";
        return new BancoDadosException("Nao foi possivel " + operacao + "." + detalhe, erro);
    }

    static void desfazer(Connection conexao, Exception original) {
        try { conexao.rollback(); }
        catch (SQLException falhaRollback) { original.addSuppressed(falhaRollback); }
    }
}

