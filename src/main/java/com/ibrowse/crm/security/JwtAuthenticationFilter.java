package com.ibrowse.crm.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIXO = "Bearer ";

	private final JwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (header != null && header.startsWith(PREFIXO)) {
			UsuarioAutenticado usuario = jwtService.validarToken(header.substring(PREFIXO.length()));
			if (usuario != null) {
				var autenticacao = new UsernamePasswordAuthenticationToken(usuario, null, usuario.authorities());
				SecurityContextHolder.getContext().setAuthentication(autenticacao);
			}
		}

		chain.doFilter(request, response);
	}

}
