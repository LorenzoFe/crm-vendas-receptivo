package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;

public record EstagioVendaResponse(
        Long id,
        String nome,
        Integer ordem,
        Integer probabilidadeConversao,
        LocalDateTime dataCriacao) {
}
