package com.ibrowse.crm.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record UsuarioAutenticado(
		Long id,
		String nome,
		String email,
		List<String> roles,
		List<String> permissoes) {

	public Collection<GrantedAuthority> authorities() {
		return Stream.concat(
				roles.stream().map(role -> "ROLE_" + role),
				permissoes.stream())
				.<GrantedAuthority>map(SimpleGrantedAuthority::new)
				.toList();
	}

}
