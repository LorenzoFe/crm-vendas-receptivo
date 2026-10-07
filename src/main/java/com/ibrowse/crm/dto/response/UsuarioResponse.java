package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UsuarioResponse(
		Long id,
		String nome,
		String email,
		String departamento,
		Boolean ativo,
		List<String> roles,
		LocalDateTime dataCriacao,
		LocalDateTime dataAtualizacao) {
}
