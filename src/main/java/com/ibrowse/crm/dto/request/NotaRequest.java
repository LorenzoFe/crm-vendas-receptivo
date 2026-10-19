package com.ibrowse.crm.dto.request;

import jakarta.validation.constraints.*;

public record NotaRequest(
        @NotBlank @Size(max = 20000) String conteudo,
        @Positive Long usuarioId,
        @Positive Long empresaId,
        @Positive Long contatoId,
        @Positive Long oportunidadeId) {
}
