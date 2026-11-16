// Todos os cadastros abaixo sao fixtures sinteticas, sem dados de pessoas reais.
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import br.com.hoteliara.dao.*;
import br.com.hoteliara.model.*;
import br.com.hoteliara.service.*;
import br.com.hoteliara.exception.*;
import br.com.hoteliara.util.SenhaUtil;

// Executar somente em uma pasta temporaria, com H2 no classpath.
public class VerificarPersistencia {
    static int checks;
    static void conferir(boolean ok, String regra) {
        if (!ok) throw new AssertionError(regra);
        checks++;
    }
    interface Acao { void executar() throws Exception; }
    static void rejeitar(Class<? extends Throwable> tipo, Acao acao) throws Exception {
        try { acao.executar(); } catch (Exception erro) {
            if (!tipo.isInstance(erro)) throw erro;
            checks++; return;
        }
        throw new AssertionError("Esperava " + tipo.getSimpleName());
    }
    public static void main(String[] args) throws Exception {
        Path configuracao = Path.of("db.properties");
        if (Files.exists(configuracao)) throw new IllegalStateException("Use uma pasta temporaria vazia.");
        Files.writeString(configuracao, "db.url=jdbc:h2:mem:iara;MODE=MySQL;DB_CLOSE_DELAY=-1\ndb.usuario=sa\ndb.senha=\n");
        try {
            try (Connection c = ConexaoBD.conectar()) {
                for (String comando : Files.readString(Path.of(args[0])).split(";")) {
                    if (!comando.isBlank()) {
                        try (PreparedStatement ps = c.prepareStatement(comando)) { ps.execute(); }
                    }
                }
            }
            PessoaDAO pessoas = new PessoaDAO();
            HotelDAO hoteis = new HotelDAO();
            CategoriaDAO categorias = new CategoriaDAO();
            QuartoDAO quartos = new QuartoDAO();
            ReservaDAO reservas = new ReservaDAO();
            PagamentoDAO pagamentos = new PagamentoDAO();
            AvaliacaoDAO avaliacoes = new AvaliacaoDAO();
            conferir(hoteis.listar().isEmpty(), "Banco vazio lista vazia");
            conferir(hoteis.buscarPorId(9999) == null, "ID ausente retorna null");
            String hash = SenhaUtil.gerarHash("123456");
            Cliente cliente = new Cliente(0, "Cliente Teste", String.format("%011d", 1), "cliente@example.test",
                    hash, "00000000000", LocalDate.of(2000, 1, 1));
            pessoas.salvar(cliente);
            conferir(cliente.getId() > 0, "ID de cliente gerado");
            conferir(pessoas.buscarPorId(cliente.getId()).autenticar("123456"), "Hash restaurado");
            Funcionario funcionario = new Funcionario(0, "Funcionario", String.format("%011d", 2), "funcionario@example.test",
                    hash, "00000000000", LocalDate.of(2000, 1, 1), "F01", "Atendente", 2000);
            pessoas.salvar(funcionario);
            conferir(pessoas.buscarPorId(funcionario.getId()) instanceof Funcionario, "Subclasse funcionario");
            Funcionario repetido = new Funcionario(0, "Duplicado", String.format("%011d", 3), "duplicado@example.test",
                    hash, "00000000000", LocalDate.of(2000, 1, 1), "F01", "Atendente", 2000);
            rejeitar(BancoDadosException.class, () -> pessoas.salvar(repetido));
            conferir(pessoas.listar().size() == 2 && repetido.getId() == 0, "Rollback em duas tabelas");
            funcionario.setCargo("Gerente");
            funcionario.setSalario(3500);
            pessoas.atualizar(funcionario);
            conferir(((Funcionario) pessoas.buscarPorId(funcionario.getId())).getSalario() == 3500,
                    "Atualiza pessoa e funcionario");
            Hotel hotel = new Hotel(0, "Hotel Teste", 4, "4133333333",
                    new Endereco("Rua Teste", "s/n", "", "80000000", "Curitiba", "PR"));
            hotel.getEndereco().setCoordenadas(-25.4, -49.2);
            hoteis.salvar(hotel);
            conferir(hotel.getId() > 0, "ID hotel");
            conferir(hoteis.buscarPorId(hotel.getId()).getEndereco().getNumero().equals("s/n"),
                    "Endereco textual");
            conferir(hoteis.buscarPorId(hotel.getId()).getEndereco().temCoordenadas(), "Coordenadas");
            hotel.setNome("Hotel Atualizado");
            hoteis.atualizar(hotel);
            conferir(hoteis.buscarPorId(hotel.getId()).getNome().equals("Hotel Atualizado"), "Atualiza hotel");
            Categoria categoria = new Categoria(0, "Padrao", TipoQuarto.PADRAO, 2, 100);
            categorias.salvar(categoria);
            conferir(categoria.getId() > 0, "ID categoria automatico");
            categoria.setValorDiaria(150);
            categorias.atualizar(categoria);
            conferir(categorias.buscarPorId(categoria.getId()).getValorDiaria() == 150, "Atualiza categoria");
            Quarto quarto = Quarto.criar(0, 101, 1, categoria, hotel);
            hotel.adicionarQuarto(quarto);
            quartos.salvar(quarto);
            conferir(quartos.buscarPorId(quarto.getId()) instanceof QuartoPadrao, "Fabrica quarto");
            LocalDate entrada = LocalDate.now().plusDays(5);
            Reserva reserva = cliente.realizarReserva(quarto, entrada, entrada.plusDays(2), 1);
            reservas.salvar(reserva);
            Reserva carregada = reservas.buscarPorId(reserva.getId());
            conferir(carregada.getCliente().consultarHistorico().get(0) == carregada, "Mesmo objeto no historico");
            conferir(carregada.getQuarto().getReservas().get(0) == carregada, "Mesmo objeto na ocupacao");
            conferir(carregada.getQuarto().getHotel() == carregada.getHotel(), "Mesmo hotel");
            conferir(!carregada.getQuarto().estaDisponivel(entrada, entrada.plusDays(1)), "Ocupacao restaurada");
            PagamentoService service = new PagamentoService();
            Pagamento pix = new PagamentoPix(reserva, 100);
            service.processar(pix);
            pagamentos.salvar(pix);
            conferir(pagamentos.buscarPorId(pix.getId()) instanceof PagamentoPix, "Fabrica pix");
            conferir(reservas.buscarPorId(reserva.getId()).getSaldoDevedor() == 200, "Saldo recuperado");
            PagamentoBoleto boleto = new PagamentoBoleto(reserva, 100);
            service.processar(boleto);
            pagamentos.salvar(boleto);
            conferir(reservas.buscarPorId(reserva.getId()).getSaldoDevedor() == 200, "Boleto pendente");
            boleto.registrarBaixa();
            pagamentos.atualizar(boleto);
            conferir(reservas.buscarPorId(reserva.getId()).getSaldoDevedor() == 100, "Baixa persistida");
            PagamentoCartao cartao = new PagamentoCartao(reserva, 100, 3);
            service.processar(cartao);
            pagamentos.salvar(cartao);
            conferir(((PagamentoCartao) pagamentos.buscarPorId(cartao.getId())).getParcelas() == 3,
                    "Parcelas restauradas");
            conferir(reservas.buscarPorId(reserva.getId()).estaQuitada(), "Reserva quitada apos leitura");
            pix.estornar();
            pagamentos.atualizar(pix);
            conferir(reservas.buscarPorId(reserva.getId()).getSaldoDevedor() == 100, "Estorno persistido");
            reserva.confirmar(); reserva.concluir();
            reservas.atualizar(reserva);
            Avaliacao avaliacao = cliente.avaliarHotel(reserva, 5, "Excelente");
            avaliacoes.salvar(avaliacao);
            conferir(hoteis.buscarPorId(hotel.getId()).calcularNotaMedia() == 5, "Media recuperada");
            avaliacao.editar(4, "Atualizada");
            avaliacoes.atualizar(avaliacao);
            conferir(avaliacoes.buscarPorId(avaliacao.getId()).getNota() == 4, "Atualiza avaliacao");
            Avaliacao duplicada = new Avaliacao(reserva, 2, "Duplicada");
            rejeitar(BancoDadosException.class, () -> avaliacoes.salvar(duplicada));
            rejeitar(BancoDadosException.class, () -> hoteis.remover(hotel.getId()));
            // Reserva passada usa restaurar e preserva valor historico, sem recalcular.
            Reserva antiga = Reserva.restaurar(0, cliente, quarto, LocalDate.of(2020,1,1),
                    LocalDate.of(2020,1,2), LocalDate.of(2020,1,3), 1, StatusReserva.CONCLUIDA, 99);
            reservas.salvar(antiga);
            conferir(reservas.buscarPorId(antiga.getId()).getValorTotal() == 99, "Reserva historica");
            reservas.remover(antiga.getId());
            conferir(reservas.buscarPorId(antiga.getId()) == null, "Remocao reserva");
            // Confirmada cobrindo hoje: banco guarda DISPONIVEL, leitura calcula OCUPADO.
            Reserva atual = Reserva.restaurar(0, cliente, quarto, LocalDate.now(),
                    LocalDate.now(), LocalDate.now().plusDays(1), 1, StatusReserva.CONFIRMADA, 150);
            reservas.salvar(atual);
            quartos.atualizar(quarto);
            conferir(quartos.buscarPorId(quarto.getId()).getStatus() == StatusQuarto.OCUPADO, "Ocupacao por data");
            try (Connection c = ConexaoBD.conectar();
                 PreparedStatement ps = c.prepareStatement("SELECT status FROM quarto WHERE id_quarto = ?")) {
                ps.setInt(1, quarto.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next(); conferir(rs.getString(1).equals("DISPONIVEL"), "Nao persiste OCUPADO");
                }
            }
            atual.cancelar(); reservas.atualizar(atual); reservas.remover(atual.getId());
            avaliacoes.remover(avaliacao.getId());
            pagamentos.remover(pix.getId()); pagamentos.remover(boleto.getId()); pagamentos.remover(cartao.getId());
            reservas.remover(reserva.getId());
            quartos.remover(quarto.getId());
            categorias.remover(categoria.getId()); hoteis.remover(hotel.getId());
            pessoas.remover(cliente.getId()); pessoas.remover(funcionario.getId());
            conferir(pessoas.listar().isEmpty() && hoteis.listar().isEmpty(), "Remocoes completas");
            System.out.println(checks + " verificacoes JDBC passaram em H2 modo MySQL.");
        } finally {
            Files.deleteIfExists(configuracao);
        }
    }
}

