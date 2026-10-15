package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;

public record ContatoResponse(
		Long id,
		String nome,
		String email,
		String telefone,
		String cargo,
		Long empresaId,
		String empresaNome,
		Long usuarioResponsavelId,
		String usuarioResponsavelNome,
		Boolean ativo,
		LocalDateTime dataCriacao,
		LocalDateTime dataAtualizacao) {
}
