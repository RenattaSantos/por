package com.estoque.estoque.UnidadeDeMedida;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository repository;

    public List<UnidadeMedidaDTO> listarTodas() {
        return repository.findAllByOrderByAbreviacaoAsc()
                .stream()
                .map(u -> new UnidadeMedidaDTO(
                        u.getId(),
                        u.getAbreviacao(),
                        u.getDescricao()
                ))
                .toList();
    }

    public UnidadeMedidaModel buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Unidade de medida não encontrada"));
    }
}