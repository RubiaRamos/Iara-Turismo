package br.com.hoteliara.controller;

import java.time.format.DateTimeFormatter;

import br.com.hoteliara.Navegador;
import br.com.hoteliara.Sessao;
import br.com.hoteliara.exception.BancoDadosException;
import br.com.hoteliara.exception.PagamentoRecusadoException;
import br.com.hoteliara.model.Pagamento;
import br.com.hoteliara.model.PagamentoBoleto;
import br.com.hoteliara.model.PagamentoCartao;
import br.com.hoteliara.model.PagamentoPix;
import br.com.hoteliara.model.Reserva;
import br.com.hoteliara.model.StatusPagamento;
import br.com.hoteliara.service.ReservaService;
import br.com.hoteliara.util.Dialogos;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/** Pagamento de uma reserva (pagamento.fxml). */
public class PagamentoController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblHotel;
    @FXML private Label lblQuarto;
    @FXML private Label lblPeriodo;
    @FXML private Label lblStatus;
    @FXML private Label lblTotal;
    @FXML private Label lblPago;
    @FXML private Label lblSaldo;
    @FXML private RadioButton opPix;
    @FXML private RadioButton opCartao;
    @FXML private RadioButton opBoleto;
    @FXML private HBox caixaParcelas;
    @FXML private ComboBox<Integer> campoParcelas;
    @FXML private TextField campoValor;

    private final ReservaService reservaService = new ReservaService();
    private Reserva reserva;

    @FXML
    private void initialize() {
        reserva = Sessao.getReservaSelecionada();
        for (int i = 1; i <= 12; i++) campoParcelas.getItems().add(i);
        campoParcelas.setValue(1);
        // As parcelas só aparecem quando a forma escolhida é cartão.
        caixaParcelas.visibleProperty().bind(opCartao.selectedProperty());
        caixaParcelas.managedProperty().bind(opCartao.selectedProperty());
        atualizarResumo();
    }

    private void atualizarResumo() {
        lblHotel.setText(reserva.getHotel().getNome());
        lblQuarto.setText("Quarto " + reserva.getQuarto().getNumero() + " — "
                + reserva.getQuarto().getCategoria().getNome() + " — "
                + reserva.getNumHospedes() + " hóspede(s)");
        lblPeriodo.setText(reserva.getCheckIn().format(DATA) + " a " + reserva.getCheckOut().format(DATA)
                + " (" + reserva.calcularNoites() + " noites)");
        lblStatus.setText("Situação: " + reserva.getStatus());
        lblTotal.setText("Total: " + Moeda.formatar(reserva.getValorTotal()));
        lblPago.setText("Já pago: " + Moeda.formatar(reserva.getValorPago()));
        lblSaldo.setText("Falta pagar: " + Moeda.formatar(reserva.getSaldoDevedor()));
        campoValor.setText(String.format("%.2f", reserva.getSaldoDevedor()).replace('.', ','));
    }

    @FXML
    private void onPagar() {
        try {
            double valor = lerValor(campoValor.getText());
            Pagamento pagamento = criarPagamento(valor);
            reservaService.pagar(reserva, pagamento);

            if (pagamento.getStatus() == StatusPagamento.PENDENTE) {
                Dialogos.informacao("Boleto gerado", "Vencimento em "
                        + ((PagamentoBoleto) pagamento).getDataVencimento().format(DATA)
                        + ". A reserva é confirmada quando o boleto for compensado.");
            } else if (reserva.estaQuitada()) {
                Dialogos.informacao("Pagamento aprovado", "Reserva quitada e confirmada!");
            } else {
                Dialogos.informacao("Pagamento aprovado", "Ainda falta pagar "
                        + Moeda.formatar(reserva.getSaldoDevedor()) + ".");
            }
            Navegador.irPara("reservas");
        } catch (NumberFormatException e) {
            Dialogos.aviso("Digite o valor só com números, por exemplo 150,00.");
        } catch (PagamentoRecusadoException e) {
            Dialogos.aviso("Pagamento recusado: " + e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            Dialogos.aviso(e.getMessage());
        } catch (BancoDadosException e) {
            Dialogos.erro("Sem conexão com o banco", e.getMessage());
        }
    }

    /**
     * Aceita "1.200,50", "1200,50" e também "1200.50" (com ponto).
     * Se tiver vírgula, ela é a casa decimal e os pontos são de milhar;
     * se não tiver, o ponto é a casa decimal.
     */
    static double lerValor(String texto) {
        String limpo = texto.replace("R$", "").replace(" ", "").trim();
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(',', '.');
        }
        return Double.parseDouble(limpo);
    }

    /**
     * A tela escolhe QUAL subclasse criar. Daí em diante, o ReservaService
     * só chama processar() e cada forma de pagamento age do seu jeito.
     */
    private Pagamento criarPagamento(double valor) throws PagamentoRecusadoException {
        if (opCartao.isSelected()) return new PagamentoCartao(reserva, valor, campoParcelas.getValue());
        if (opBoleto.isSelected()) return new PagamentoBoleto(reserva, valor);
        return new PagamentoPix(reserva, valor);
    }

    @FXML
    private void onDepois() {
        Navegador.irPara("reservas");
    }

    @FXML
    private void onVoltar() {
        Navegador.irPara("pesquisa");
    }
}
