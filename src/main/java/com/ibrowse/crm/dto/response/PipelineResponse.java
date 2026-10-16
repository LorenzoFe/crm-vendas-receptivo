package com.ibrowse.crm.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PipelineResponse(
        List<PipelineEstagioResponse> estagios, long quantidadeTotal,
        BigDecimal valorEstimadoTotal, BigDecimal valorPonderadoTotal) {
}
