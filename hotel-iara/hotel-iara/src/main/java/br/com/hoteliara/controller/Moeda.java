package br.com.hoteliara.controller;

import java.text.NumberFormat;
import java.util.Locale;

/** Formata valores em reais para as telas: 1234.5 -> "R$ 1.234,50". */
final class Moeda {

    private static final NumberFormat REAL = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));

    private Moeda() {
    }

    static String formatar(double valor) {
        return REAL.format(valor).replace(' ', ' ');
    }
}
