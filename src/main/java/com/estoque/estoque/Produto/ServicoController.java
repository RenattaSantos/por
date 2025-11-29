package com.estoque.estoque.Produto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ProdutoService service;

    @PostMapping
    public ResponseEntity<ProdutoModel> criar(@Valid @RequestBody ServicoRequest req) {
        var salvo = service.criarServico(req);
        return ResponseEntity
                .created(URI.create("/api/produtos/" + salvo.getId_produto()))
                .body(salvo);
    }

    @PutMapping("/{id}")
    public ProdutoModel atualizar(@PathVariable Long id, @Valid @RequestBody ServicoRequest req) {
        return service.atualizarServico(id, req);
    }
}
