package com.estoque.estoque.Produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServicoRequest(
        @NotBlank @Size(max = 50) String nomeProduto,
        @NotBlank @Size(max = 250) String descricao_produto
) {}
