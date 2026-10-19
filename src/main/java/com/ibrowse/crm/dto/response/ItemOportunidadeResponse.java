package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ItemOportunidadeResponse(
        Long id, Long oportunidadeId, Long produtoId, String produtoNome,
        Integer quantidade, BigDecimal precoUnitario, BigDecimal subtotal,
        LocalDateTime dataCriacao) {
}
