package br.com.hoteliara.dao;

import br.com.hoteliara.exception.BancoDadosException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexaoBD {

    // Classe utilitaria: nao e necessario criar um objeto para abrir a conexao.
    private ConexaoBD() {
    }

    public static Connection conectar() {
        Properties configuracao = new Properties();

        // Fecha o arquivo automaticamente, inclusive se ocorrer uma excecao.
        try (InputStream arquivo = Files.newInputStream(Path.of("db.properties"))) {
            configuracao.load(arquivo);
        } catch (IOException erro) {
            throw new BancoDadosException(
                    "Nao foi possivel ler db.properties. Confira o arquivo e o diretorio de execucao.", erro);
        }

        String url = exigir(configuracao, "db.url");
        String usuario = exigir(configuracao, "db.usuario");
        String senha = configuracao.getProperty("db.senha");
        if (senha == null) {
            throw new BancoDadosException("Falta a propriedade db.senha em db.properties.",
                    new IllegalArgumentException("Propriedade ausente: db.senha"));
        }

        try {
            // O DAO que recebe a conexao sera responsavel por fecha-la.
            return DriverManager.getConnection(url, usuario, senha);
        } catch (SQLException erro) {
            throw new BancoDadosException(
                    "Nao foi possivel conectar ao banco. Confira o MySQL e as configuracoes de acesso.", erro);
        }
    }

    private static String exigir(Properties configuracao, String chave) {
        String valor = configuracao.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            throw new BancoDadosException("Falta preencher " + chave + " em db.properties.",
                    new IllegalArgumentException("Propriedade ausente ou vazia: " + chave));
        }
        return valor.trim();
    }
}
