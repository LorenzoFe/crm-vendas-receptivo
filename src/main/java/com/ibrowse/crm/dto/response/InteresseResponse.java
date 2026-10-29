package com.ibrowse.crm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.ibrowse.crm.enums.StatusDiscador;
import com.ibrowse.crm.enums.StatusInteresse;

public record InteresseResponse(
        Long id, String nome, String email, String telefone, String cargo,
        String empresaNome, String cnpj, String segmento, String tamanhoEmpresa, String site,
        String preferenciaContato, String mensagem, StatusInteresse status,
        List<ProdutoPublicoResponse> produtos,
        Long oportunidadeId, String usuarioTratamentoNome, LocalDateTime dataTratamento,
        StatusDiscador discadorStatus, Integer discadorTentativas, String discadorErro,
        LocalDateTime discadorEnviadoEm, LocalDateTime discadorResultadoEm,
        LocalDateTime dataConsentimento, LocalDateTime dataCriacao) {
}
