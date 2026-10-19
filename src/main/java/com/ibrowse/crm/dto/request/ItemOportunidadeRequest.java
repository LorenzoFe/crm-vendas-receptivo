package com.ibrowse.crm.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemOportunidadeRequest(
        @NotNull @Positive Long produtoId,
        @NotNull @Positive Integer quantidade,
        @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precoUnitario) {
}
