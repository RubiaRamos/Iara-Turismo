// Todos os cadastros abaixo sao fixtures sinteticas, sem dados de pessoas reais.
import java.time.LocalDate;
import br.com.hoteliara.model.*;
import br.com.hoteliara.service.PagamentoService;
import br.com.hoteliara.exception.PagamentoRecusadoException;
public class VerificarDominio {
    static int checks;
    static void conferir(boolean condicao, String regra) {
        if (!condicao) throw new AssertionError(regra);
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
        Endereco endereco = new Endereco("Rua Teste", "10", "", "80000000", "Curitiba", "PR");
        Hotel hotel = new Hotel(1, "Hotel Teste", 3, "", endereco);
        Categoria categoria = new Categoria(1, "Padrao", TipoQuarto.PADRAO, 2, 100);
        Quarto quarto = Quarto.criar(1, 101, 1, categoria, hotel);
        hotel.adicionarQuarto(quarto);
        Cliente cliente = new Cliente(1, "Teste", String.format("%011d", 1), "teste@example.test",
                "hash-de-teste", "00000000000", LocalDate.of(2000, 1, 1));
        LocalDate entrada = LocalDate.now().plusDays(10);
        Reserva reserva = cliente.realizarReserva(quarto, entrada, entrada.plusDays(10), 1);
        PagamentoService service = new PagamentoService();
        Pagamento pix = new PagamentoPix(reserva, 200);
        conferir(reserva.getPagamentos().isEmpty(), "Construtor sem associacao");
        service.processar(pix);
        conferir(reserva.getSaldoDevedor() == 800, "Pix reduz saldo");
        rejeitar(IllegalStateException.class, () -> service.processar(pix));
        rejeitar(PagamentoRecusadoException.class, () -> new PagamentoPix(reserva, 0));
        rejeitar(PagamentoRecusadoException.class, () -> new PagamentoPix(reserva, Double.NaN));
        rejeitar(PagamentoRecusadoException.class, () -> new PagamentoCartao(reserva, 10, 13));
        Pagamento alto = new PagamentoPix(reserva, 801);
        rejeitar(PagamentoRecusadoException.class, () -> service.processar(alto));
        conferir(!reserva.getPagamentos().contains(alto), "Recusa nao associa");
        PagamentoBoleto boleto = new PagamentoBoleto(reserva, 300);
        service.processar(boleto);
        conferir(boleto.getStatus() == StatusPagamento.PENDENTE && reserva.getSaldoDevedor() == 800,
                "Boleto pendente nao reduz saldo");
        rejeitar(IllegalStateException.class, boleto::estornar);
        boleto.registrarBaixa();
        conferir(reserva.getSaldoDevedor() == 500, "Baixa reduz saldo");
        pix.cancelar();
        conferir(pix.getStatus() == StatusPagamento.ESTORNADO && reserva.getSaldoDevedor() == 700,
                "Cancelavel estorna");
        service.processar(new PagamentoCartao(reserva, 700, 12));
        conferir(reserva.estaQuitada(), "Cartao quita");
        PagamentoBoleto atrasado = new PagamentoBoleto(reserva, 50);
        rejeitar(PagamentoRecusadoException.class, atrasado::registrarBaixa);
        Pagamento antigo = Pagamento.restaurar(77, reserva, "PIX", 200, entrada.minusDays(1),
                StatusPagamento.ESTORNADO, null, null);
        conferir(antigo.getId() == 77 && antigo.getStatus() == StatusPagamento.ESTORNADO,
                "Restauracao preserva historico sem reprocessar saldo");
        rejeitar(IllegalStateException.class, () -> new Avaliacao(reserva, 5, "Teste"));
        reserva.confirmar();
        reserva.concluir();
        Avaliacao avaliacao = cliente.avaliarHotel(reserva, 5, "Boa estadia");
        conferir(hotel.calcularNotaMedia() == 5, "Integracao Cliente Hotel Avaliacao");
        rejeitar(IllegalArgumentException.class, () -> avaliacao.editar(0, "Invalida"));
        conferir(avaliacao.getNota() == 5, "Edicao invalida preserva nota");
        avaliacao.editar(4, "Revisao");
        conferir(hotel.calcularNotaMedia() == 4, "Media reflete edicao");
        System.out.println(checks + " verificacoes passaram.");
    }
}

