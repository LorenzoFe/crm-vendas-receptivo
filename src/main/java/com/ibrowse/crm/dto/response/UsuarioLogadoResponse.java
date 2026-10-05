package com.ibrowse.crm.dto.response;

import java.util.List;

public record UsuarioLogadoResponse(
		Long id,
		String nome,
		String email,
		List<String> roles,
		List<String> permissoes) {
}
