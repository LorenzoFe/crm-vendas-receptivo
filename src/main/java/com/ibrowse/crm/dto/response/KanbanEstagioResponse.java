package com.ibrowse.crm.dto.response;

import java.util.List;

public record KanbanEstagioResponse(
        Long id, String nome, Integer ordem, Integer probabilidadeConversao,
        List<OportunidadeResponse> oportunidades) {
}
