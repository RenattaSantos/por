package com.estoque.estoque.Produto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoModel, Long> {
    boolean existsByNomeProdutoIgnoreCase(String nomeProduto);

    // para excluir diretamente
    long deleteByNomeProdutoIgnoreCase(String nomeProduto);

    // opcional: buscar por nome (se quiser conferir antes)
    Optional<ProdutoModel> findByNomeProdutoIgnoreCase(String nomeProduto);
}
