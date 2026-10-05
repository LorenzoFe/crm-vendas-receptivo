package com.ibrowse.crm.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.LoginRequest;
import com.ibrowse.crm.dto.response.LoginResponse;
import com.ibrowse.crm.dto.response.UsuarioLogadoResponse;
import com.ibrowse.crm.entity.Permissao;
import com.ibrowse.crm.entity.Role;
import com.ibrowse.crm.entity.Usuario;
import com.ibrowse.crm.repository.UsuarioRepository;
import com.ibrowse.crm.security.JwtService;
import com.ibrowse.crm.security.UsuarioAutenticado;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final String CREDENCIAIS_INVALIDAS = "Email ou senha inválidos";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(request.email().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS));

		if (usuario.getSenha() == null || !passwordEncoder.matches(request.senha(), usuario.getSenha())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
		}

		if (!Boolean.TRUE.equals(usuario.getAtivo())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo");
		}

		UsuarioAutenticado autenticado = paraUsuarioAutenticado(usuario);
		JwtService.TokenGerado token = jwtService.gerarToken(autenticado);

		return new LoginResponse(token.token(), "Bearer", token.expiraEm(), paraResponse(autenticado));
	}

	public UsuarioLogadoResponse paraResponse(UsuarioAutenticado usuario) {
		return new UsuarioLogadoResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.roles(), usuario.permissoes());
	}

	private UsuarioAutenticado paraUsuarioAutenticado(Usuario usuario) {
		List<String> roles = usuario.getRoles().stream()
				.map(Role::getNome)
				.sorted()
				.toList();

		List<String> permissoes = usuario.getRoles().stream()
				.flatMap(role -> role.getPermissoes().stream())
				.map(Permissao::getNome)
				.distinct()
				.sorted()
				.toList();

		return new UsuarioAutenticado(usuario.getId(), usuario.getNome(), usuario.getEmail(), roles, permissoes);
	}

}
