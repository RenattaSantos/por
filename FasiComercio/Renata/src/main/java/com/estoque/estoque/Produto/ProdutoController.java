package com.estoque.estoque.Produto;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProdutoModel> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ProdutoModel buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProdutoModel> criar(@Valid @RequestBody ProdutoModel produto) {
        ProdutoModel salvo = service.criar(produto);
        return ResponseEntity.created(URI.create("/api/produtos/" + salvo.getId_produto())).body(salvo);
    }

    @PutMapping("/{id}")
    public ProdutoModel atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoModel produto) {
        return service.atualizar(id, produto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE por nome: /api/produtos/nome?nome=Mouse%20Gamer%20RGB
    @DeleteMapping("/nome")
    public ResponseEntity<Void> excluirPorNome(@RequestParam("nome") String nome) {
        service.excluirPorNome(nome);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/servicos")
    public ResponseEntity<ProdutoModel> criarServico(@Valid @RequestBody ServicoRequest req) {
        var salvo = service.criarServico(req);
        return ResponseEntity.created(URI.create("/api/produtos/" + salvo.getId_produto()))
                .body(salvo);
    }

}
