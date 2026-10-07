package com.ibrowse.crm.controller;

import com.ibrowse.crm.dto.request.AlterarRolesRequest;
import com.ibrowse.crm.dto.request.UsuarioRequest;
import com.ibrowse.crm.dto.request.UsuarioUpdateRequest;
import com.ibrowse.crm.dto.response.UsuarioResponse;
import com.ibrowse.crm.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('gerenciar_usuarios')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuários", description = "Cadastro de usuários internos do CRM")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Lista usuários (paginado)")
    public PagedModel<UsuarioResponse> listar(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable){
        return new PagedModel<>(usuarioService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca o usuário por ID")
    public UsuarioResponse buscarPorId(@PathVariable Long id){
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um usuário")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request){
        UsuarioResponse criado = usuarioService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, e-mail e departamento")
    public UsuarioResponse atualizar(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequest request){
        return usuarioService.atualizar(id, request);
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "Define as roles do usuário")
    public UsuarioResponse alterarRoles(@PathVariable Long id, @RequestBody @Valid AlterarRolesRequest request){
        return usuarioService.alterarRoles(id, request.roles());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativa um usuário (exclusão lógica)")
    public ResponseEntity<Void> desativar(@PathVariable Long id){
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
