package br.com.hoteliara.modelo;

/**
 * Dados de um quarto exibido nas telas.
 *
 * @param id             identificador usado pelos cards (quarto1..quarto4)
 * @param nome           nome do quarto
 * @param descricao      texto principal (pode ter mais de um parágrafo)
 * @param destaque       frase em negrito antes da lista de comodidades
 * @param comodidades    lista de comodidades em uma única linha
 * @param imagem         caminho da foto grande usada na tela de detalhe
 * @param tituloImagem   imagem do título em letra cursiva (pode ser null)
 */
public record Quarto(
        String id,
        String nome,
        String descricao,
        String destaque,
        String comodidades,
        String imagem,
        String tituloImagem) {
}
