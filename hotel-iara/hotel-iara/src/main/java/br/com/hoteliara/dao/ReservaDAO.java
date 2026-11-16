package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class ReservaDAO implements Dao<Reserva> {
    @Override
    public void salvar(Reserva objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO reserva (id_cliente, id_quarto, data_reserva, check_in, check_out, num_hospedes, status, valor_total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Reserva buscarPorId(int id) {
        return DadosBD.carregar().reservas.get(id);
    }

    @Override
    public List<Reserva> listar() {
        return new ArrayList<>(DadosBD.carregar().reservas.values());
    }

    @Override
    public void atualizar(Reserva objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE reserva SET id_cliente = ?, id_quarto = ?, data_reserva = ?, check_in = ?, check_out = ?, num_hospedes = ?, status = ?, valor_total = ? WHERE id_reserva = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM reserva WHERE id_reserva = ?", Jdbc.id(id));
    }

    private Object[] valores(Reserva objeto) {
        return new Object[] {Jdbc.id(objeto.getCliente().getId()), Jdbc.id(objeto.getQuarto().getId()),
                objeto.getDataReserva(), objeto.getCheckIn(), objeto.getCheckOut(),
                objeto.getNumHospedes(), objeto.getStatus().name(), objeto.getValorTotal()};
    }
}

