package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;

public record HistoricoResponse(
        Long id, String origem, String tipo, String conteudo,
        LocalDateTime dataEvento, Long usuarioId) {
}
