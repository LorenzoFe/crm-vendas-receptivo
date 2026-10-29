package com.ibrowse.crm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.ibrowse.crm.dto.request.InteresseRequest;
import com.ibrowse.crm.dto.response.ProdutoPublicoResponse;
import com.ibrowse.crm.service.InteresseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/publico")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Área do Cliente (pública)")
public class PublicoController {

    private final InteresseService service;

    @GetMapping("/produtos")
    @Operation(summary = "Produtos ativos que o visitante pode escolher")
    public List<ProdutoPublicoResponse> produtos() {
        return service.produtosDisponiveis();
    }

    @PostMapping("/interesses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra o interesse de um visitante (fica como NOVO até a equipe tratar)")
    public void registrarInteresse(@RequestBody @Valid InteresseRequest request) {
        service.registrar(request);
    }
}
