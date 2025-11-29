package com.estoque.estoque.UnidadeDeMedida;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UNIMEDIDA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadeMedidaModel {

    @Id
    @Column(name = "IDUNMEDI")
    private Long id;

    @Column(name = "DESCRICAO", nullable = false, length = 100)
    private String descricao;

    @Column(name = "UNIABREV", nullable = false, length = 10)
    private String abreviacao;
}
