package br.com.hoteliara.dao;

import java.util.List;

// O contrato e comum; cada DAO implementa o SQL da sua entidade.
public interface Dao<T> {

    void salvar(T objeto);

    // Retorna null quando o identificador nao existe.
    T buscarPorId(int id);

    // Retorna uma lista vazia quando nao ha registros.
    List<T> listar();

    void atualizar(T objeto);

    void remover(int id);
}
