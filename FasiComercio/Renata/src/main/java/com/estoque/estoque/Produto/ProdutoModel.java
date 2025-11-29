package com.estoque.estoque.Produto;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = ProdutoModel.NOME_TABELA)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class ProdutoModel {

    public static final String NOME_TABELA = "PRODUTO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDPRODUTO", unique = true, nullable = false)
    private Long id_produto;

    @Column(name = "NOME", unique = true, nullable = false, length = 50)
    @NotBlank
    @Size(max=50)
    private String nomeProduto;

    @Column(name = "TEMPIDEAL")
    private Double temperatura_produto;

    @Column(name = "DESCRICAO", nullable = false, length = 250)
    @NotBlank
    @Size(max=250)
    private String descricao_produto;

    @Column(name = "CODBARRAS")
    @NotNull
    private String codg_barras_prod;

    @Column(name = "STQMAX", nullable = false)
    @Min(0)
    private int estoque_maximo;

    @Column(name = "STQMIN", nullable = false)
    @Min(0)
    private int estoque_minimo;

    @Column(name = "PNTPEDIDO", nullable = false)
    @Min(0)
    private int ponto_abastecimento;

    @Column(name = "ID_ALMOX", nullable = true)
    private Long id_almoxarifado;

    @Column(name = "ID_UNMEDI", nullable = false)
    @NotNull
    private Long id_unmedida;
}
