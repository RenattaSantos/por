package com.estoque.estoque.Produto;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
public class ProdutoService {

    private final ProdutoRepository repo;

    // EAN-13 sintético/constante para serviços (válido)
    private static final String EAN_SERVICO_FIXO = "9999999999996";

    // Defaults para colunas NOT NULL quando o item é SERVIÇO
    private static final int DEFAULT_STQ_MIN = 0;
    private static final int DEFAULT_STQ_MAX = 1;
    private static final int DEFAULT_PONTO  = 1;

    // Unidade de Medida fixa para SERVIÇO (ID=1)
    private static final long UOM_SERVICO_ID = 1L;

    public ProdutoService(ProdutoRepository repo) {
        this.repo = repo;
    }

    // ===== LISTAGENS =====

    // Lista "crua" de ProdutoModel (se precisar em algum lugar)
    public List<ProdutoModel> listar() {
        return repo.findAll();
    }

    // Lista detalhada (DTO com unidade_medida, temperatura etc.)
    public List<ProdutoDetalhesDTO> listarDetalhado() {
        return repo.listarComUnidade();
    }

    // ===== CRUD PRODUTO =====

    public ProdutoModel buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Produto não encontrado"));
    }

    @Transactional
    public ProdutoModel criar(ProdutoModel p) {
        if (p.getId_produto() != null) {
            throw new ResponseStatusException(BAD_REQUEST, "ID não deve ser informado na criação");
        }

        // normalizações
        if (p.getNomeProduto() != null) {
            p.setNomeProduto(p.getNomeProduto().trim());
        }
        if (p.getNomeProduto() == null || p.getNomeProduto().isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Nome é obrigatório");
        }

        if (repo.existsByNomeProdutoIgnoreCase(p.getNomeProduto())) {
            throw new ResponseStatusException(BAD_REQUEST, "Já existe produto com esse nome");
        }

        // Unicidade de EAN para PRODUTO (ignora se for o EAN fixo de serviço)
        String ean = p.getCodg_barras_prod();
        if (ean == null || ean.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Código de barras é obrigatório");
        }
        if (!EAN_SERVICO_FIXO.equals(ean)) {
            if (repo.existsByBarcode(ean)) {
                throw new ResponseStatusException(BAD_REQUEST, "Já existe produto com este código de barras");
            }
        }

        validarRegras(p);
        return repo.save(p);
    }

    @Transactional
    public ProdutoModel atualizar(Long id, ProdutoModel body) {
        ProdutoModel atual = buscar(id);

        // nome
        String nome = body.getNomeProduto() != null ? body.getNomeProduto().trim() : null;
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Nome é obrigatório");
        }

        repo.findByNomeProdutoIgnoreCase(nome)
                .filter(p -> !p.getId_produto().equals(id))
                .ifPresent(p -> {
                    throw new ResponseStatusException(BAD_REQUEST, "Já existe produto com esse nome");
                });

        // Unicidade de EAN (exclui o próprio registro) — ignora se for EAN fixo de serviço
        String ean = body.getCodg_barras_prod();
        if (ean == null || ean.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Código de barras é obrigatório");
        }
        if (!EAN_SERVICO_FIXO.equals(ean)) {
            repo.findByBarcode(ean)
                    .filter(p -> !p.getId_produto().equals(id))
                    .ifPresent(p -> {
                        throw new ResponseStatusException(BAD_REQUEST, "Já existe produto com este código de barras");
                    });
        }

        // aplica campos
        atual.setNomeProduto(nome);
        atual.setTemperatura_produto(body.getTemperatura_produto());
        atual.setDescricao_produto(body.getDescricao_produto());
        atual.setCodg_barras_prod(ean);
        atual.setEstoque_maximo(body.getEstoque_maximo());
        atual.setEstoque_minimo(body.getEstoque_minimo());
        atual.setPonto_abastecimento(body.getPonto_abastecimento());
        atual.setId_almoxarifado(body.getId_almoxarifado());
        atual.setId_unmedida(body.getId_unmedida());

        validarRegras(atual);
        return repo.save(atual);
    }

    @Transactional
    public void excluir(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Produto não encontrado");
        }
        repo.deleteById(id);
    }

    private void validarRegras(ProdutoModel p) {
        if (p.getEstoque_minimo() < 0 || p.getEstoque_maximo() < 0 || p.getPonto_abastecimento() < 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Valores de estoque não podem ser negativos");
        }
        if (p.getEstoque_maximo() <= p.getEstoque_minimo()) {
            throw new ResponseStatusException(BAD_REQUEST, "Estoque máximo deve ser MAIOR que o estoque mínimo");
        }
        if (p.getPonto_abastecimento() <= p.getEstoque_minimo()
                || p.getPonto_abastecimento() > p.getEstoque_maximo()) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "Ponto de pedido deve ser > estoque mínimo e ≤ estoque máximo"
            );
        }
    }

    @Transactional
    public void excluirPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(BAD_REQUEST, "Informe o nome do produto");
        }
        String nomeLimpo = nome.strip();

        long apagados = repo.deleteByNomeProdutoIgnoreCase(nomeLimpo);
        if (apagados == 0) {
            throw new ResponseStatusException(NOT_FOUND, "Produto não encontrado pelo nome");
        }
    }

    // ====== SERVIÇO: força UoM ID=1 e demais defaults ======

    @Transactional
    public ProdutoModel criarServico(ServicoRequest in) {
        String nome = in.nomeProduto().trim();
        if (repo.existsByNomeProdutoIgnoreCase(nome)) {
            throw new ResponseStatusException(BAD_REQUEST, "Já existe item com esse nome");
        }

        var m = new ProdutoModel();
        m.setId_produto(null);
        m.setNomeProduto(nome);
        m.setDescricao_produto(in.descricao_produto().trim());

        m.setTemperatura_produto(null);
        m.setId_almoxarifado(null);

        m.setEstoque_minimo(DEFAULT_STQ_MIN);
        m.setEstoque_maximo(DEFAULT_STQ_MAX);
        m.setPonto_abastecimento(DEFAULT_PONTO);

        m.setId_unmedida(UOM_SERVICO_ID);
        m.setCodg_barras_prod(EAN_SERVICO_FIXO);

        return repo.save(m);
    }

    @Transactional
    public ProdutoModel atualizarServico(Long id, ServicoRequest in) {
        var atual = buscar(id);

        String nome = in.nomeProduto().trim();
        repo.findByNomeProdutoIgnoreCase(nome)
                .filter(p -> !p.getId_produto().equals(id))
                .ifPresent(p -> {
                    throw new ResponseStatusException(BAD_REQUEST, "Já existe item com esse nome");
                });

        atual.setNomeProduto(nome);
        atual.setDescricao_produto(in.descricao_produto().trim());

        atual.setTemperatura_produto(null);
        atual.setId_almoxarifado(null);

        atual.setEstoque_minimo(DEFAULT_STQ_MIN);
        atual.setEstoque_maximo(DEFAULT_STQ_MAX);
        atual.setPonto_abastecimento(DEFAULT_PONTO);

        atual.setId_unmedida(UOM_SERVICO_ID);
        atual.setCodg_barras_prod(EAN_SERVICO_FIXO);

        return repo.save(atual);
    }
}
