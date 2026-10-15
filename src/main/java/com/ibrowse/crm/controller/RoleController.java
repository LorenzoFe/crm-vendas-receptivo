package com.ibrowse.crm.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibrowse.crm.dto.response.PermissaoResponse;
import com.ibrowse.crm.dto.response.RoleResponse;
import com.ibrowse.crm.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('gerenciar_usuarios')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Roles e permissões", description = "Consulta dos papéis e do que cada um pode fazer")
public class RoleController {

	private final RoleService roleService;

	@GetMapping("/roles")
	@Operation(summary = "Lista as roles com suas permissões")
	public List<RoleResponse> listarRoles() {
		return roleService.listarRoles();
	}

	@GetMapping("/permissoes")
	@Operation(summary = "Lista todas as permissões do sistema")
	public List<PermissaoResponse> listarPermissoes() {
		return roleService.listarPermissoes();
	}

}
