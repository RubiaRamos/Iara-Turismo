package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class HotelDAO implements Dao<Hotel> {
    // Atualiza somente a localizacao, sem sobrescrever outros campos do hotel.
    public void atualizarCoordenadas(int id, double latitude, double longitude) {
        Jdbc.id(id);
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)
            throw new IllegalArgumentException("Coordenadas invalidas.");
        try (java.sql.Connection c = ConexaoBD.conectar();
             java.sql.PreparedStatement ps = c.prepareStatement(
                     "UPDATE hotel SET latitude = ?, longitude = ? WHERE id_hotel = ?")) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setInt(3, id);
            if (ps.executeUpdate() == 0)
                throw new IllegalArgumentException("Hotel nao encontrado para salvar coordenadas.");
        } catch (java.sql.SQLException erro) {
            throw Jdbc.falha("salvar as coordenadas", erro);
        }
    }

    @Override
    public void salvar(Hotel objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO hotel (nome, classificacao, telefone, logradouro, numero, complemento, cep, cidade, estado, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Hotel buscarPorId(int id) {
        return DadosBD.carregar().hoteis.get(id);
    }

    @Override
    public List<Hotel> listar() {
        return new ArrayList<>(DadosBD.carregar().hoteis.values());
    }

    @Override
    public void atualizar(Hotel objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE hotel SET nome = ?, classificacao = ?, telefone = ?, logradouro = ?, numero = ?, complemento = ?, cep = ?, cidade = ?, estado = ?, latitude = ?, longitude = ? WHERE id_hotel = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM hotel WHERE id_hotel = ?", Jdbc.id(id));
    }

    private Object[] valores(Hotel objeto) {
        Endereco e = objeto.getEndereco();
        return new Object[] {objeto.getNome(), objeto.getClassificacao(), objeto.getTelefone(),
                e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getCep(),
                e.getCidade(), e.getEstado(), e.getLatitude(), e.getLongitude()};
    }
}

