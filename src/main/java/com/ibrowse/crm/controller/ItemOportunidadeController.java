package com.ibrowse.crm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ibrowse.crm.dto.request.AtualizarItemRequest;
import com.ibrowse.crm.dto.request.ItemOportunidadeRequest;
import com.ibrowse.crm.dto.response.ItemOportunidadeResponse;
import com.ibrowse.crm.service.ItemOportunidadeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/oportunidades/{oportunidadeId}/itens")
@RequiredArgsConstructor
@Validated
@Tag(name = "Itens da oportunidade")
public class ItemOportunidadeController {

    private final ItemOportunidadeService service;

    @GetMapping
    public List<ItemOportunidadeResponse> listar(@PathVariable @Positive Long oportunidadeId) {
        return service.listar(oportunidadeId);
    }

    @PostMapping
    public ResponseEntity<ItemOportunidadeResponse> adicionar(@PathVariable @Positive Long oportunidadeId,
            @RequestBody @Valid ItemOportunidadeRequest request) {
        return ResponseEntity.status(201).body(service.adicionar(oportunidadeId, request));
    }

    @PutMapping("/{itemId}")
    public ItemOportunidadeResponse atualizar(@PathVariable @Positive Long oportunidadeId,
            @PathVariable @Positive Long itemId, @RequestBody @Valid AtualizarItemRequest request) {
        return service.atualizar(oportunidadeId, itemId, request);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> remover(@PathVariable @Positive Long oportunidadeId,
            @PathVariable @Positive Long itemId) {
        service.remover(oportunidadeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
