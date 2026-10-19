package com.ibrowse.crm.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OportunidadeRequest(
        @NotBlank @Size(max = 255) String titulo,
        String descricao,
        @NotNull @Positive Long empresaId,
        @Positive Long contatoId,
        @NotNull @Positive Long vendedorId,
        @Positive Long estagioId,
        @DecimalMin("0.00") @Digits(integer = 13, fraction = 2) BigDecimal valorEstimado,
        LocalDate dataFechamentoEsperada) {

    public OportunidadeRequest {
        titulo = titulo == null ? null : titulo.strip();
    }
}
