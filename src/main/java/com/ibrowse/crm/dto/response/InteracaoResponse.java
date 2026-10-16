package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;
import com.ibrowse.crm.enums.TipoInteracao;

public record InteracaoResponse(
        Long id, TipoInteracao tipo, String descricao, LocalDateTime dataInteracao,
        Long usuarioId, String usuarioNome,
        Long empresaId, Long contatoId, Long oportunidadeId, LocalDateTime dataCriacao) {
}
