package com.ibrowse.crm.dto.response;

import java.util.List;

public record OportunidadeCompletaResponse(
        OportunidadeResponse oportunidade, EmpresaResumoResponse empresa,
        ContatoResumoResponse contato, List<ItemOportunidadeResponse> itens,
        List<PropostaResponse> propostas) {
}
