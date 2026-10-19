package com.ibrowse.crm.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ibrowse.crm.dto.response.PropostaResponse;
import com.ibrowse.crm.enums.StatusProposta;
import com.ibrowse.crm.service.PropostaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/propostas")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Propostas", description = "Propostas comerciais das oportunidades")
public class PropostaController {

    private final PropostaService propostaService;

    @PostMapping("/gerar-de-oportunidade/{id}")
    @PreAuthorize("hasAuthority('criar_proposta')")
    @Operation(summary = "Gera um rascunho a partir dos itens de uma oportunidade ativa")
    public ResponseEntity<PropostaResponse> gerarDeOportunidade(@PathVariable Long id) {
        PropostaResponse criada = propostaService.gerarDeOportunidade(id);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/propostas/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    @Operation(summary = "Lista propostas com filtros opcionais de status e oportunidade")
    public PagedModel<PropostaResponse> listar(@RequestParam(required = false) StatusProposta status,
            @RequestParam(required = false) Long oportunidadeId,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return new PagedModel<>(propostaService.listar(status, oportunidadeId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public PropostaResponse buscarPorId(@PathVariable Long id) {
        return propostaService.buscarPorId(id);
    }

    @PutMapping("/{id}/enviar")
    @PreAuthorize("hasAuthority('editar_proposta')")
    public PropostaResponse enviar(@PathVariable Long id) {
        return propostaService.enviar(id);
    }

    @PutMapping("/{id}/aceitar")
    @PreAuthorize("hasAuthority('editar_proposta')")
    public PropostaResponse aceitar(@PathVariable Long id) {
        return propostaService.aceitar(id);
    }

    @PutMapping("/{id}/rejeitar")
    @PreAuthorize("hasAuthority('editar_proposta')")
    public PropostaResponse rejeitar(@PathVariable Long id) {
        return propostaService.rejeitar(id);
    }
}
