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

    public List<ProdutoModel> listar() {
        return repo.findAll();
    }

    public ProdutoModel buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Produto não encontrado"));
    }

    @Transactional
    public ProdutoModel criar(ProdutoModel p) {
        validarRegras(p);

        if (p.getId_produto() != null) {
            throw new ResponseStatusException(BAD_REQUEST, "ID não deve ser informado na criação");
        }
        if (repo.existsByNomeProdutoIgnoreCase(p.getNomeProduto())) {
            throw new ResponseStatusException(BAD_REQUEST, "Já existe produto com esse nome");
        }
        return repo.save(p);
    }

    @Transactional
    public ProdutoModel atualizar(Long id, ProdutoModel body) {
        ProdutoModel atual = buscar(id);

        atual.setNomeProduto(body.getNomeProduto());
        atual.setTemperatura_produto(body.getTemperatura_produto());
        atual.setDescricao_produto(body.getDescricao_produto());
        atual.setCodg_barras_prod(body.getCodg_barras_prod());
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

        if (repo.existsByNomeProdutoIgnoreCase(in.nomeProduto())) {
            throw new ResponseStatusException(BAD_REQUEST, "Já existe item com esse nome");
        }

        var m = new ProdutoModel();
        m.setId_produto(null);
        m.setNomeProduto(in.nomeProduto());
        m.setDescricao_produto(in.descricao_produto());

        // Serviço não usa temperatura/almox
        m.setTemperatura_produto(null);
        m.setId_almoxarifado(null);

        // Preencher colunas NOT NULL de estoque com defaults mínimos
        m.setEstoque_minimo(DEFAULT_STQ_MIN);
        m.setEstoque_maximo(DEFAULT_STQ_MAX);
        m.setPonto_abastecimento(DEFAULT_PONTO);

        // Unidade de medida OBRIGATÓRIA no banco -> sempre 1L para SERVIÇO
        m.setId_unmedida(UOM_SERVICO_ID);

        // Serviço: CODBARRAS fixo e válido para cumprir NOT NULL
        m.setCodg_barras_prod(EAN_SERVICO_FIXO);

        // Não chamar validarRegras() aqui: regra de estoque é diferente para serviço.
        return repo.save(m);
    }

    @Transactional
    public ProdutoModel atualizarServico(Long id, ServicoRequest in) {
        // 1) Buscar existente
        var atual = buscar(id);

        // 2) Garantir que é tratado como SERVIÇO (reforçar defaults)
        //    - Unicidade de nome (excluindo o próprio item)
        repo.findByNomeProdutoIgnoreCase(in.nomeProduto())
                .filter(p -> !p.getId_produto().equals(id))
                .ifPresent(p -> { throw new ResponseStatusException(BAD_REQUEST, "Já existe item com esse nome"); });

        // 3) Atualizar apenas campos editáveis de serviço
        atual.setNomeProduto(in.nomeProduto());
        atual.setDescricao_produto(in.descricao_produto());

        // 4) Reforçar regras de serviço a cada update
        atual.setTemperatura_produto(null);
        atual.setId_almoxarifado(null);

        atual.setEstoque_minimo(DEFAULT_STQ_MIN);  // 0
        atual.setEstoque_maximo(DEFAULT_STQ_MAX);  // 1
        atual.setPonto_abastecimento(DEFAULT_PONTO); // 1

        atual.setId_unmedida(UOM_SERVICO_ID);      // 1L
        atual.setCodg_barras_prod(EAN_SERVICO_FIXO); // "9999999999996"

        // 5) NÃO chamar validarRegras(): serviço tem regra própria
        return repo.save(atual);
    }

}
