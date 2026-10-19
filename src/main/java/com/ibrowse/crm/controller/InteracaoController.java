package com.ibrowse.crm.controller;

import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.ibrowse.crm.dto.request.InteracaoRequest;
import com.ibrowse.crm.dto.response.InteracaoResponse;
import com.ibrowse.crm.service.InteracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/interacoes")
@RequiredArgsConstructor
@Tag(name = "Interações")
public class InteracaoController {

    private final InteracaoService service;

    @GetMapping
    public PagedModel<InteracaoResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public InteracaoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<InteracaoResponse> criar(@RequestBody @Valid InteracaoRequest request) {
        InteracaoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public InteracaoResponse atualizar(@PathVariable Long id, @RequestBody @Valid InteracaoRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
