package com.ibrowse.crm.controller;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ibrowse.crm.dto.request.MudarEstagioRequest;
import com.ibrowse.crm.dto.request.OportunidadeRequest;
import com.ibrowse.crm.dto.request.PerderOportunidadeRequest;
import com.ibrowse.crm.dto.response.KanbanEstagioResponse;
import com.ibrowse.crm.dto.response.OportunidadeCompletaResponse;
import com.ibrowse.crm.dto.response.OportunidadeResponse;
import com.ibrowse.crm.dto.response.PipelineResponse;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.service.OportunidadeConsultaService;
import com.ibrowse.crm.service.OportunidadeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/oportunidades")
@RequiredArgsConstructor
@Validated
@Tag(name = "Oportunidades")
public class OportunidadeController {

    private final OportunidadeService service;
    private final OportunidadeConsultaService consultas;

    @GetMapping
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    @Operation(summary = "Lista oportunidades com filtros e paginação")
    public PagedModel<OportunidadeResponse> listar(
            @RequestParam(required = false) StatusOportunidade status,
            @RequestParam(required = false) @Positive Long vendedorId,
            @RequestParam(required = false) @Positive Long estagioId,
            @RequestParam(required = false) @Positive Long empresaId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(consultas.listar(status, vendedorId, estagioId, empresaId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public OportunidadeResponse buscar(@PathVariable @Positive Long id) {
        return consultas.buscar(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('criar_oportunidade')")
    @Operation(summary = "Cria oportunidade ATIVA; usa estágio inicial se omitido")
    public ResponseEntity<OportunidadeResponse> criar(@RequestBody @Valid OportunidadeRequest request) {
        OportunidadeResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('editar_oportunidade')")
    public OportunidadeResponse atualizar(@PathVariable @Positive Long id,
            @RequestBody @Valid OportunidadeRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('deletar_oportunidade')")
    @Operation(summary = "Exclui oportunidade ATIVA sem registros dependentes")
    public ResponseEntity<Void> excluir(@PathVariable @Positive Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/mudar-estagio")
    @PreAuthorize("hasAuthority('editar_oportunidade')")
    public OportunidadeResponse mudarEstagio(@PathVariable @Positive Long id,
            @RequestBody @Valid MudarEstagioRequest request) {
        return service.mudarEstagio(id, request);
    }

    @PutMapping("/{id}/perder")
    @PreAuthorize("hasAuthority('editar_oportunidade')")
    public OportunidadeResponse perder(@PathVariable @Positive Long id,
            @RequestBody @Valid PerderOportunidadeRequest request) {
        return service.perder(id, request);
    }

    @GetMapping("/kanban")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public List<KanbanEstagioResponse> kanban(@RequestParam(required = false) @Positive Long vendedorId) {
        return consultas.kanban(vendedorId);
    }

    @GetMapping("/pipeline")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public PipelineResponse pipeline() {
        return consultas.pipeline();
    }

    @GetMapping("/{id}/completa")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public OportunidadeCompletaResponse completa(@PathVariable @Positive Long id) {
        return consultas.completa(id);
    }
}
