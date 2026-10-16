package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstagioVendaRequest(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotNull(message = "Ordem é obrigatória")
        Integer ordem,

        @NotNull(message = "Probabilidade de conversão é obrigatória")
        @Min(value = 0, message = "Probabilidade deve ser no mínimo 0")
        @Max(value = 100, message = "Probabilidade deve ser no máximo 100")
        Integer probabilidadeConversao) {

    public EstagioVendaRequest {
        nome = nome == null ? null : nome.strip();
    }
}
