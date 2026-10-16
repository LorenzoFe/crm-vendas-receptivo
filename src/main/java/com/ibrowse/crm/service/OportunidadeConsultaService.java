package com.ibrowse.crm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.response.*;
import com.ibrowse.crm.config.PaginacaoConfig;
import com.ibrowse.crm.entity.EstagioVenda;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.mapper.OportunidadeMapper;
import com.ibrowse.crm.repository.EstagioVendaRepository;
import com.ibrowse.crm.repository.OportunidadeRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OportunidadeConsultaService {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "titulo", "valorEstimado",
            "dataCriacao", "dataAtualizacao", "dataFechamentoEsperada", "status");

    private final OportunidadeRepository repository;
    private final EstagioVendaRepository estagios;
    private final OportunidadeMapper mapper;

    public Page<OportunidadeResponse> listar(StatusOportunidade status, Long vendedorId, Long estagioId,
            Long empresaId, Pageable pageable) {
        PaginacaoConfig.validar(pageable, CAMPOS_ORDENACAO);
        return repository.filtrar(status, vendedorId, estagioId, empresaId, pageable).map(mapper::toResponse);
    }

    public OportunidadeResponse buscar(Long id) {
        return mapper.toResponse(encontrar(id));
    }

    public List<KanbanEstagioResponse> kanban(Long vendedorId) {
        Map<Long, List<OportunidadeResponse>> porEstagio = repository
                .buscarParaKanban(StatusOportunidade.ATIVA, vendedorId).stream()
                .map(mapper::toResponse).collect(Collectors.groupingBy(OportunidadeResponse::estagioId));
        return estagios.findAllByOrderByOrdemAscIdAsc().stream()
                .map(estagio -> new KanbanEstagioResponse(estagio.getId(), estagio.getNome(),
                        estagio.getOrdem(), estagio.getProbabilidadeConversao(),
                        porEstagio.getOrDefault(estagio.getId(), List.of())))
                .toList();
    }

    private Oportunidade encontrar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade " + id + " não encontrada"));
    }
}
