package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class CategoriaDAO implements Dao<Categoria> {
    @Override
    public void salvar(Categoria objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO categoria (nome, tipo, capacidade, valor_diaria) VALUES (?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Categoria buscarPorId(int id) {
        return DadosBD.carregar().categorias.get(id);
    }

    @Override
    public List<Categoria> listar() {
        return new ArrayList<>(DadosBD.carregar().categorias.values());
    }

    @Override
    public void atualizar(Categoria objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE categoria SET nome = ?, tipo = ?, capacidade = ?, valor_diaria = ? WHERE id_categoria = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM categoria WHERE id_categoria = ?", Jdbc.id(id));
    }

    private Object[] valores(Categoria objeto) {
        return new Object[] {objeto.getNome(), objeto.getTipo().name(),
                objeto.getCapacidade(), objeto.getValorDiaria()};
    }
}

