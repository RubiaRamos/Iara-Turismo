package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class AvaliacaoDAO implements Dao<Avaliacao> {
    @Override
    public void salvar(Avaliacao objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO avaliacao (id_reserva, nota, comentario, data_avaliacao) VALUES (?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Avaliacao buscarPorId(int id) {
        return DadosBD.carregar().avaliacoes.get(id);
    }

    @Override
    public List<Avaliacao> listar() {
        return new ArrayList<>(DadosBD.carregar().avaliacoes.values());
    }

    @Override
    public void atualizar(Avaliacao objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE avaliacao SET id_reserva = ?, nota = ?, comentario = ?, data_avaliacao = ? WHERE id_avaliacao = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM avaliacao WHERE id_avaliacao = ?", Jdbc.id(id));
    }

    private Object[] valores(Avaliacao objeto) {
        return new Object[] {Jdbc.id(objeto.getReserva().getId()), objeto.getNota(),
                objeto.getComentario(), objeto.getData()};
    }
}

