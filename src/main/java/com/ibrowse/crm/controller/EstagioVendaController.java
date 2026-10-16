package com.ibrowse.crm.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ibrowse.crm.dto.request.EstagioVendaRequest;
import com.ibrowse.crm.dto.response.EstagioVendaResponse;
import com.ibrowse.crm.service.EstagioVendaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/estagios")
@RequiredArgsConstructor
@Tag(name = "Estágios", description = "Etapas do funil de vendas")
public class EstagioVendaController {

    private final EstagioVendaService estagioVendaService;

    @GetMapping
    @Operation(summary = "Lista estágios por ordem e id")
    public List<EstagioVendaResponse> listar() {
        return estagioVendaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um estágio pelo id")
    public EstagioVendaResponse buscarPorId(@PathVariable Long id) {
        return estagioVendaService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um estágio")
    public ResponseEntity<EstagioVendaResponse> criar(@RequestBody @Valid EstagioVendaRequest request) {
        EstagioVendaResponse criado = estagioVendaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um estágio")
    public EstagioVendaResponse atualizar(@PathVariable Long id,
            @RequestBody @Valid EstagioVendaRequest request) {
        return estagioVendaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um estágio sem oportunidades vinculadas")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        estagioVendaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
