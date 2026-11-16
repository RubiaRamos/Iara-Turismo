package br.com.hoteliara.dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import br.com.hoteliara.model.*;
import br.com.hoteliara.exception.*;

// Um carregamento usa uma instancia por ID; nao recria a mesma reserva em cada DAO.
final class DadosBD {
    final Map<Integer, Hotel> hoteis = new LinkedHashMap<>();
    final Map<Integer, Categoria> categorias = new LinkedHashMap<>();
    final Map<Integer, Quarto> quartos = new LinkedHashMap<>();
    final Map<Integer, Pessoa> pessoas = new LinkedHashMap<>();
    final Map<Integer, Reserva> reservas = new LinkedHashMap<>();
    final Map<Integer, Pagamento> pagamentos = new LinkedHashMap<>();
    final Map<Integer, Avaliacao> avaliacoes = new LinkedHashMap<>();

    private interface Leitor { void ler(ResultSet rs) throws SQLException,
            QuartoIndisponivelException, PagamentoRecusadoException; }

    private static void consultar(Connection c, String sql, Leitor leitor)
            throws SQLException, QuartoIndisponivelException, PagamentoRecusadoException {
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) leitor.ler(rs);
        }
    }

    private static LocalDate data(ResultSet rs, String coluna) throws SQLException {
        Date valor = rs.getDate(coluna);
        return valor == null ? null : valor.toLocalDate();
    }

    private static <T> T exigir(Map<Integer, T> mapa, int id, String entidade) {
        T objeto = mapa.get(id);
        if (objeto == null)
            throw new IllegalStateException("Referencia ausente no banco: " + entidade + " #" + id);
        return objeto;
    }

    static DadosBD carregar() {
        try (Connection c = ConexaoBD.conectar()) {
            // As oito consultas enxergam uma mesma versao dos dados.
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            c.setAutoCommit(false);
            try {
                DadosBD dados = carregar(c);
                c.commit();
                return dados;
            } catch (SQLException | QuartoIndisponivelException | PagamentoRecusadoException
                    | RuntimeException erro) {
                Jdbc.desfazer(c, erro);
                throw new BancoDadosException("Nao foi possivel reconstruir os dados do hotel. "
                        + "Confira o esquema e os registros vinculados.", erro);
            }
        } catch (SQLException erro) {
            throw Jdbc.falha("ler os dados", erro);
        }
    }

    private static DadosBD carregar(Connection c)
            throws SQLException, QuartoIndisponivelException, PagamentoRecusadoException {
        DadosBD d = new DadosBD();
        Set<Integer> quartosEmManutencao = new HashSet<>();
        consultar(c, "SELECT * FROM hotel ORDER BY id_hotel", rs -> {
            Endereco e = new Endereco(rs.getString("logradouro"), rs.getString("numero"),
                    rs.getString("complemento"), rs.getString("cep"),
                    rs.getString("cidade"), rs.getString("estado"));
            Object lat = rs.getObject("latitude"), lon = rs.getObject("longitude");
            if (lat != null && lon != null)
                e.setCoordenadas(((Number) lat).doubleValue(), ((Number) lon).doubleValue());
            Hotel hotel = new Hotel(rs.getInt("id_hotel"), rs.getString("nome"),
                    rs.getInt("classificacao"), rs.getString("telefone"), e);
            d.hoteis.put(hotel.getId(), hotel);
        });
        consultar(c, "SELECT * FROM categoria ORDER BY id_categoria", rs -> {
            Categoria categoria = new Categoria(rs.getInt("id_categoria"), rs.getString("nome"),
                    TipoQuarto.valueOf(rs.getString("tipo")), rs.getInt("capacidade"),
                    rs.getDouble("valor_diaria"));
            d.categorias.put(categoria.getId(), categoria);
        });
        consultar(c, "SELECT * FROM quarto ORDER BY id_quarto", rs -> {
            Hotel hotel = exigir(d.hoteis, rs.getInt("id_hotel"), "hotel");
            Categoria categoria = exigir(d.categorias, rs.getInt("id_categoria"), "categoria");
            Quarto quarto = Quarto.criar(rs.getInt("id_quarto"), rs.getInt("numero"),
                    rs.getInt("andar"), categoria, hotel);
            // Restaurar reservas existentes nao deve ser bloqueado pela manutencao atual.
            if ("MANUTENCAO".equals(rs.getString("status"))) quartosEmManutencao.add(quarto.getId());
            hotel.adicionarQuarto(quarto);
            d.quartos.put(quarto.getId(), quarto);
        });
        consultar(c, "SELECT p.*, f.matricula, f.cargo, f.salario FROM pessoa p "
                + "LEFT JOIN funcionario f ON f.id_pessoa = p.id_pessoa ORDER BY p.id_pessoa", rs -> {
            int id = rs.getInt("id_pessoa");
            Pessoa pessoa;
            if ("CLIENTE".equals(rs.getString("tipo"))) {
                pessoa = new Cliente(id, rs.getString("nome"), rs.getString("cpf"),
                        rs.getString("email"), rs.getString("senha_hash"),
                        rs.getString("telefone"), data(rs, "dt_nasc"));
            } else if ("FUNCIONARIO".equals(rs.getString("tipo"))) {
                pessoa = new Funcionario(id, rs.getString("nome"), rs.getString("cpf"),
                        rs.getString("email"), rs.getString("senha_hash"),
                        rs.getString("telefone"), data(rs, "dt_nasc"),
                        rs.getString("matricula"), rs.getString("cargo"), rs.getDouble("salario"));
            } else throw new IllegalStateException("Tipo de pessoa desconhecido.");
            d.pessoas.put(id, pessoa);
        });
        consultar(c, "SELECT * FROM reserva ORDER BY id_reserva", rs -> {
            Pessoa pessoa = exigir(d.pessoas, rs.getInt("id_cliente"), "cliente");
            if (!(pessoa instanceof Cliente cliente))
                throw new IllegalStateException("Uma reserva precisa pertencer a um cliente.");
            Quarto quarto = exigir(d.quartos, rs.getInt("id_quarto"), "quarto");
            Reserva reserva = Reserva.restaurar(rs.getInt("id_reserva"), cliente, quarto,
                    data(rs, "data_reserva"), data(rs, "check_in"), data(rs, "check_out"),
                    rs.getInt("num_hospedes"), StatusReserva.valueOf(rs.getString("status")),
                    rs.getDouble("valor_total"));
            cliente.adicionarReservaAoHistorico(reserva);
            d.reservas.put(reserva.getId(), reserva);
        });
        // Mantem todas as reservas e so depois reaplica o estado fisico do quarto.
        for (int id : quartosEmManutencao) d.quartos.get(id).colocarEmManutencao();
        consultar(c, "SELECT * FROM pagamento ORDER BY id_pagamento", rs -> {
            Reserva reserva = exigir(d.reservas, rs.getInt("id_reserva"), "reserva");
            Object parcelas = rs.getObject("parcelas");
            Pagamento pagamento = Pagamento.restaurar(rs.getInt("id_pagamento"), reserva,
                    rs.getString("forma"), rs.getDouble("valor"), data(rs, "data_pagamento"),
                    StatusPagamento.valueOf(rs.getString("status")),
                    parcelas == null ? null : ((Number) parcelas).intValue(),
                    data(rs, "data_vencimento"));
            reserva.adicionarPagamento(pagamento);
            d.pagamentos.put(pagamento.getId(), pagamento);
        });
        consultar(c, "SELECT * FROM avaliacao ORDER BY id_avaliacao", rs -> {
            Reserva reserva = exigir(d.reservas, rs.getInt("id_reserva"), "reserva");
            Avaliacao avaliacao = Avaliacao.restaurar(rs.getInt("id_avaliacao"), reserva,
                    rs.getInt("nota"), rs.getString("comentario"), data(rs, "data_avaliacao"));
            reserva.getHotel().adicionarAvaliacao(avaliacao);
            d.avaliacoes.put(avaliacao.getId(), avaliacao);
        });
        return d;
    }
}

