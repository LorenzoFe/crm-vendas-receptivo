package com.ibrowse.crm.dto.request;

import java.time.LocalDateTime;
import com.ibrowse.crm.enums.TipoInteracao;
import jakarta.validation.constraints.*;

public record InteracaoRequest(
        @NotNull TipoInteracao tipo,
        @NotBlank @Size(max = 20000) String descricao,
        @NotNull LocalDateTime dataInteracao,
        @NotNull @Positive Long usuarioId,
        @Positive Long empresaId,
        @Positive Long contatoId,
        @Positive Long oportunidadeId) {
}
