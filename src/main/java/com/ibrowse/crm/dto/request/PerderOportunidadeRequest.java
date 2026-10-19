package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerderOportunidadeRequest(@NotBlank @Size(max = 255) String motivo) {

    public PerderOportunidadeRequest {
        motivo = motivo == null ? null : motivo.strip();
    }
}
