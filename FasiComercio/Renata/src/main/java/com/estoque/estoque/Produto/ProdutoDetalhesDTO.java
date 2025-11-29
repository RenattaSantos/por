package com.estoque.estoque.Produto;

import lombok.Data;

@Data
public class ProdutoDetalhesDTO {
    private Long id_produto;
    private String nomeProduto;
    private String codg_barras_prod;
    private int quantidade_em_estoque;
    private String unidade_medida;
}