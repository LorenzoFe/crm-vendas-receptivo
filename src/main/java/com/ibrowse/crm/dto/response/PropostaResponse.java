package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ibrowse.crm.enums.StatusProposta;

public record PropostaResponse(
        Long id,
        String numero,
        Long oportunidadeId,
        BigDecimal valorTotal,
        StatusProposta status,
        LocalDate dataEmissao,
        LocalDate dataVencimento,
        LocalDate dataAceitacao,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao) {
}
