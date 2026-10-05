package com.ibrowse.crm.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibrowse.crm.dto.request.LoginRequest;
import com.ibrowse.crm.dto.response.LoginResponse;
import com.ibrowse.crm.dto.response.UsuarioLogadoResponse;
import com.ibrowse.crm.security.UsuarioAutenticado;
import com.ibrowse.crm.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Login e dados do usuário logado")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@Operation(summary = "Faz login e devolve o token JWT")
	public LoginResponse login(@RequestBody @Valid LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	@SecurityRequirement(name = "bearerAuth")
	@Operation(summary = "Dados do usuário dono do token")
	public UsuarioLogadoResponse me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return authService.paraResponse(usuario);
	}

}
