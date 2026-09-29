package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
		Long id,
		String nome,
		String descricao,
		BigDecimal precoUnitario,
		String categoria,
		Boolean ativo,
		LocalDateTime dataCriacao,
		LocalDateTime dataAtualizacao) {
}
