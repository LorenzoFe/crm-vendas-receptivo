package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.ibrowse.crm.enums.StatusOportunidade;

public record OportunidadeResponse(
        Long id, String titulo, String descricao,
        Long empresaId, String empresaNome,
        Long contatoId, String contatoNome,
        Long vendedorId, String vendedorNome,
        Long estagioId, String estagioNome,
        BigDecimal valorEstimado, BigDecimal valorReal,
        StatusOportunidade status, String motivoPerda,
        LocalDate dataFechamentoEsperada, LocalDate dataFechamentoReal,
        LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
}
