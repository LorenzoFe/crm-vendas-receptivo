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
import com.ibrowse.crm.repository.PipelineResumo;

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

    public PipelineResponse pipeline() {
        Map<Long, PipelineResumo> resumo = repository.resumirPipeline(StatusOportunidade.ATIVA).stream()
                .collect(Collectors.toMap(PipelineResumo::getEstagioId, Function.identity()));
        List<PipelineEstagioResponse> linhas = estagios.findAllByOrderByOrdemAscIdAsc().stream()
                .map(estagio -> linhaPipeline(estagio, resumo.get(estagio.getId()))).toList();
        return new PipelineResponse(linhas,
                linhas.stream().mapToLong(PipelineEstagioResponse::quantidade).sum(),
                linhas.stream().map(PipelineEstagioResponse::valorEstimado).reduce(BigDecimal.ZERO, BigDecimal::add),
                linhas.stream().map(PipelineEstagioResponse::valorPonderado).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private PipelineEstagioResponse linhaPipeline(EstagioVenda estagio, PipelineResumo resumo) {
        BigDecimal valor = resumo == null || resumo.getValorEstimado() == null
                ? BigDecimal.ZERO : resumo.getValorEstimado();
        int probabilidade = estagio.getProbabilidadeConversao() == null ? 0 : estagio.getProbabilidadeConversao();
        BigDecimal ponderado = valor.multiply(BigDecimal.valueOf(probabilidade))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return new PipelineEstagioResponse(estagio.getId(), estagio.getNome(),
                resumo == null ? 0 : resumo.getQuantidade(), valor.setScale(2, RoundingMode.HALF_UP), ponderado);
    }

    private Oportunidade encontrar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade " + id + " não encontrada"));
    }
}
