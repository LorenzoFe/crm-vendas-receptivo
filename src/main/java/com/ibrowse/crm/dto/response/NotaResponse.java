package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;

public record NotaResponse(
        Long id, String conteudo, Long usuarioId, String usuarioNome,
        Long empresaId, Long contatoId, Long oportunidadeId,
        LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
}
