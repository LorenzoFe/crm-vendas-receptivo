package com.ibrowse.crm.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ibrowse.crm.dto.response.InteresseResponse;
import com.ibrowse.crm.enums.StatusInteresse;
import com.ibrowse.crm.service.InteresseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("/interesses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Interessados", description = "Interesses recebidos pela Área do Cliente")
public class InteresseController {

    private final InteresseService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public PagedModel<InteresseResponse> listar(
            @RequestParam(required = false) StatusInteresse status,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(service.listar(status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ver_oportunidade')")
    public InteresseResponse buscar(@PathVariable @Positive Long id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}/converter")
    @PreAuthorize("hasAuthority('criar_oportunidade') and hasAuthority('criar_empresa')")
    @Operation(summary = "Cria (ou reaproveita) empresa e contato e abre uma oportunidade no primeiro estágio")
    public InteresseResponse converter(@PathVariable @Positive Long id) {
        return service.converter(id);
    }

    @PutMapping("/{id}/reenviar-discador")
    @PreAuthorize("hasAuthority('editar_oportunidade')")
    @Operation(summary = "Envia de novo o interesse para a fila do discador (Vonix)",
            description = "Se a Vonix recusar, retorna 200 com discadorStatus ERRO e a mensagem em discadorErro.")
    public InteresseResponse reenviarDiscador(@PathVariable @Positive Long id) {
        return service.reenviarDiscador(id);
    }

    @PutMapping("/{id}/descartar")
    @PreAuthorize("hasAuthority('editar_oportunidade')")
    public InteresseResponse descartar(@PathVariable @Positive Long id) {
        return service.descartar(id);
    }
}
