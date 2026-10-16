package com.ibrowse.crm.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ibrowse.crm.enums.PrioridadeTarefa;
import com.ibrowse.crm.enums.StatusTarefa;

public record TarefaResponse(
        Long id, String titulo, String descricao,
        Long responsavelId, String responsavelNome,
        StatusTarefa status, PrioridadeTarefa prioridade,
        LocalDate dataVencimento, LocalDateTime dataConclusao,
        Long empresaId, Long contatoId, Long oportunidadeId,
        LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
}
