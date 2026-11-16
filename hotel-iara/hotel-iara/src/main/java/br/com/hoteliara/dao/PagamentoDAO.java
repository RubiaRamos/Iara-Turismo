package br.com.hoteliara.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import br.com.hoteliara.model.*;

public class PagamentoDAO implements Dao<Pagamento> {
    @Override
    public void salvar(Pagamento objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        if (objeto.getId() != 0) throw new IllegalArgumentException("Objeto ja possui ID; use atualizar.");
        int id = Jdbc.inserir("INSERT INTO pagamento (id_reserva, forma, valor, status, data_pagamento, parcelas, data_vencimento) VALUES (?, ?, ?, ?, ?, ?, ?)", valores(objeto));
        objeto.setId(id);
    }

    @Override
    public Pagamento buscarPorId(int id) {
        return DadosBD.carregar().pagamentos.get(id);
    }

    @Override
    public List<Pagamento> listar() {
        return new ArrayList<>(DadosBD.carregar().pagamentos.values());
    }

    @Override
    public void atualizar(Pagamento objeto) {
        if (objeto == null) throw new IllegalArgumentException("Objeto e obrigatorio.");
        Object[] campos = valores(objeto);
        Object[] parametros = Arrays.copyOf(campos, campos.length + 1);
        parametros[campos.length] = Jdbc.id(objeto.getId());
        Jdbc.executar("UPDATE pagamento SET id_reserva = ?, forma = ?, valor = ?, status = ?, data_pagamento = ?, parcelas = ?, data_vencimento = ? WHERE id_pagamento = ?", parametros);
    }

    @Override
    public void remover(int id) {
        Jdbc.executar("DELETE FROM pagamento WHERE id_pagamento = ?", Jdbc.id(id));
    }

    private Object[] valores(Pagamento objeto) {
        String forma;
        Integer parcelas = null;
        java.time.LocalDate vencimento = null;
        // Aqui precisamos do tipo para escolher os campos da tabela, nao para processar.
        if (objeto instanceof PagamentoPix) forma = "PIX";
        else if (objeto instanceof PagamentoCartao cartao) {
            forma = "CARTAO";
            parcelas = cartao.getParcelas();
        } else if (objeto instanceof PagamentoBoleto boleto) {
            forma = "BOLETO";
            vencimento = boleto.getDataVencimento();
        } else throw new IllegalArgumentException("Forma de pagamento nao suportada pelo banco.");
        return new Object[] {Jdbc.id(objeto.getReserva().getId()), forma, objeto.getValor(),
                objeto.getStatus().name(), objeto.getData(), parcelas, vencimento};
    }
}

