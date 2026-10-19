package com.ibrowse.crm.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;
import com.ibrowse.crm.dto.response.HistoricoResponse;
import com.ibrowse.crm.service.HistoricoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/oportunidades/{id}/historico")
@RequiredArgsConstructor
@Tag(name = "Histórico", description = "Interações e notas vinculadas diretamente à oportunidade")
public class HistoricoController {

    private final HistoricoService service;

    @GetMapping
    public PagedModel<HistoricoResponse> listar(@PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return new PagedModel<>(service.listar(id, pageable));
    }
}
