package com.ibrowse.crm.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	private final SecretKey chave;
	private final long expiracaoMs;

	public JwtService(@Value("${jwt.secret}") String segredo, @Value("${jwt.expiration-ms}") long expiracaoMs) {
		this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
		this.expiracaoMs = expiracaoMs;
	}

	public TokenGerado gerarToken(UsuarioAutenticado usuario) {
		Instant agora = Instant.now();
		Instant expiraEm = agora.plusMillis(expiracaoMs);

		String token = Jwts.builder()
				.subject(usuario.email())
				.claim("id", usuario.id())
				.claim("nome", usuario.nome())
				.claim("roles", usuario.roles())
				.claim("permissoes", usuario.permissoes())
				.issuedAt(Date.from(agora))
				.expiration(Date.from(expiraEm))
				.signWith(chave)
				.compact();

		return new TokenGerado(token, expiraEm);
	}

	public UsuarioAutenticado validarToken(String token) {
		try {
			Claims claims = Jwts.parser()
					.verifyWith(chave)
					.build()
					.parseSignedClaims(token)
					.getPayload();

			return new UsuarioAutenticado(
					claims.get("id", Long.class),
					claims.get("nome", String.class),
					claims.getSubject(),
					lista(claims.get("roles")),
					lista(claims.get("permissoes")));
		} catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}

	private List<String> lista(Object valor) {
		if (valor instanceof List<?> itens) {
			return itens.stream().map(String::valueOf).toList();
		}
		return List.of();
	}

	public record TokenGerado(String token, Instant expiraEm) {
	}

}
