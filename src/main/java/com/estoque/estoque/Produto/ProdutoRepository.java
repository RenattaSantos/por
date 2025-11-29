package com.estoque.estoque.Produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoModel, Long> {

    boolean existsByNomeProdutoIgnoreCase(String nomeProduto);

    long deleteByNomeProdutoIgnoreCase(String nomeProduto);

    Optional<ProdutoModel> findByNomeProdutoIgnoreCase(String nomeProduto);

    @Query("select (count(p) > 0) from ProdutoModel p where p.codg_barras_prod = :ean")
    boolean existsByBarcode(@Param("ean") String ean);

    @Query("select p from ProdutoModel p where p.codg_barras_prod = :ean")
    Optional<ProdutoModel> findByBarcode(@Param("ean") String ean);

    // ===== Listagem com unidade de medida (UNIABREV) + temperatura =====
    @Query("""
        select new com.estoque.estoque.Produto.ProdutoDetalhesDTO(
            p.id_produto,
            p.nomeProduto,
            p.descricao_produto,
            p.codg_barras_prod,
            p.temperatura_produto,
            p.estoque_minimo,
            p.estoque_maximo,
            p.ponto_abastecimento,
            u.abreviacao
        )
        from ProdutoModel p
        join UnidadeMedidaModel u
          on u.id = p.id_unmedida
        order by p.id_produto
        """)
    List<ProdutoDetalhesDTO> listarComUnidade();
}
