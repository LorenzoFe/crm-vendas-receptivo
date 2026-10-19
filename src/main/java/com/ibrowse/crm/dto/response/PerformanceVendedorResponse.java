package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PerformanceVendedorResponse(
		Long usuarioId,
		String usuarioNome,
		LocalDate inicio,
		LocalDate fim,
		long oportunidadesCriadas,
		long oportunidadesAtivas,
		BigDecimal valorEmAberto,
		long ganhas,
		long perdidas,
		BigDecimal taxaConversao,
		BigDecimal totalVendido,
		BigDecimal ticketMedio,
		BigDecimal cicloMedioDias) {
}
