package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;

public record EmpresaResponse(
		Long id,
		String nomeRazaoSocial,
		String cnpj,
		String segmento,
		String tamanhoEmpresa,
		String telefone,
		String email,
		String site,
		Boolean ativo,
		Long usuarioResponsavelId,
		String usuarioResponsavelNome,
		LocalDateTime dataCriacao,
		LocalDateTime dataAtualizacao) {
}
