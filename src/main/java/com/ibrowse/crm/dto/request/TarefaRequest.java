package com.ibrowse.crm.dto.request;

import java.time.LocalDate;
import com.ibrowse.crm.enums.PrioridadeTarefa;
import com.ibrowse.crm.enums.StatusTarefa;
import jakarta.validation.constraints.*;

public record TarefaRequest(
        @NotBlank @Size(max = 255) String titulo,
        @Size(max = 20000) String descricao,
        @NotNull @Positive Long responsavelId,
        StatusTarefa status,
        @NotNull PrioridadeTarefa prioridade,
        LocalDate dataVencimento,
        @Positive Long empresaId,
        @Positive Long contatoId,
        @Positive Long oportunidadeId) {
}
