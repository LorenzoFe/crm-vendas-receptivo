package com.ibrowse.crm.controller;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibrowse.crm.discador.DiscadorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@PreAuthorize("hasAuthority('gerenciar_permissoes')")
@RequestMapping("/discador")
@RequiredArgsConstructor
@Tag(name = "Discador", description = "Integração com a fila de discagem da Vonix")
public class DiscadorController {

    private final DiscadorService service;

    @GetMapping("/situacao")
    @Operation(summary = "Mostra se o discador está ligado e configurado (sem expor o token)")
    public Map<String, Object> situacao() {
        return service.situacao();
    }

    @GetMapping(value = "/filas", produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_PLAIN_VALUE })
    @Operation(summary = "Lista as filas da Vonix que o token pode alimentar (resposta original da Vonix)")
    public String filas() {
        return service.listarFilas();
    }
}
