package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class QuartoDAO implements Dao<Quarto> {
    @Override
    public void salvar(Quarto objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO quarto (id_hotel, id_categoria, numero, andar, status) VALUES (?, ?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Quarto buscarPorId(int id) {
        return DadosBD.carregar().quartos.get(id);
    }

    @Override
    public List<Quarto> listar() {
        return new ArrayList<>(DadosBD.carregar().quartos.values());
    }

    @Override
    public void atualizar(Quarto objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE quarto SET id_hotel = ?, id_categoria = ?, numero = ?, andar = ?, status = ? WHERE id_quarto = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM quarto WHERE id_quarto = ?", Jdbc.id(id));
    }

    private Object[] valores(Quarto objeto) {
        // OCUPADO e calculado pelas reservas; nao e persistido.
        String status = objeto.getStatus() == StatusQuarto.MANUTENCAO ? "MANUTENCAO" : "DISPONIVEL";
        return new Object[] {Jdbc.id(objeto.getHotel().getId()), Jdbc.id(objeto.getCategoria().getId()),
                objeto.getNumero(), objeto.getAndar(), status};
    }
}

