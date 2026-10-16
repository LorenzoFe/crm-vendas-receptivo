package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;

public record PipelineEstagioResponse(
        Long estagioId, String nome, long quantidade,
        BigDecimal valorEstimado, BigDecimal valorPonderado) {
}
