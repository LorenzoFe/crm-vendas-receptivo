package com.ibrowse.crm.controller;

import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.ibrowse.crm.dto.request.TarefaRequest;
import com.ibrowse.crm.dto.response.TarefaResponse;
import com.ibrowse.crm.service.TarefaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@PessoaBController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas")
public class TarefaController {

    private final TarefaService service;

    @GetMapping
    public PagedModel<TarefaResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(service.listar(pageable));
    }

    @GetMapping("/{id}")
    public TarefaResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<TarefaResponse> criar(@RequestBody @Valid TarefaRequest request) {
        TarefaResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    public TarefaResponse atualizar(@PathVariable Long id, @RequestBody @Valid TarefaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/urgentes")
    public PagedModel<TarefaResponse> urgentes(@RequestParam(defaultValue = "5") int dias,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(service.urgentes(dias, pageable));
    }

    @PutMapping("/{id}/concluir")
    public TarefaResponse concluir(@PathVariable Long id) {
        return service.concluir(id);
    }
}
