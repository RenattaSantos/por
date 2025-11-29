package com.estoque.estoque.UnidadeDeMedida;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
public class UnidadeMedidaController {

    private final UnidadeMedidaService service;

    /**
     * Lista todas as unidades de medida cadastradas.
     *
     * GET /api/unidades-medida
     */
    @GetMapping
    public List<UnidadeMedidaDTO> listar() {
        return service.listarTodas();
    }

    /**
     * (Opcional) Buscar uma unidade específica por ID.
     *
     * GET /api/unidades-medida/{id}
     */
    @GetMapping("/{id}")
    public UnidadeMedidaDTO buscar(@PathVariable Long id) {
        var u = service.buscarPorId(id);
        return new UnidadeMedidaDTO(u.getId(), u.getAbreviacao(), u.getDescricao());
    }
}