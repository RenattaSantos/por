package com.estoque.estoque.Produto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoDetalhesDTO {

    private Long id_produto;
    private String nomeProduto;
    private String descricao_produto;
    private String codg_barras_prod;

    private Double temperatura_produto; // aparece no front

    private int estoque_minimo;
    private int estoque_maximo;
    private int ponto_abastecimento;

    private String unidade_medida; // UNIABREV da UNIMEDIDA
}
