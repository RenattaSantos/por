package com.estoque.estoque.UnidadeDeMedida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadeMedidaRepository extends JpaRepository<UnidadeMedidaModel, Long> {

    // Lista todas ordenadas pela sigla (UNIABREV)
    List<UnidadeMedidaModel> findAllByOrderByAbreviacaoAsc();
}