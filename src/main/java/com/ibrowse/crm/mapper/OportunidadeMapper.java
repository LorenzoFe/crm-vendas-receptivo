package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.response.OportunidadeResponse;
import com.ibrowse.crm.entity.Oportunidade;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OportunidadeMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "empresaNome", source = "empresa.nomeRazaoSocial")
    @Mapping(target = "contatoId", source = "contato.id")
    @Mapping(target = "contatoNome", source = "contato.nome")
    @Mapping(target = "vendedorId", source = "vendedor.id")
    @Mapping(target = "vendedorNome", source = "vendedor.nome")
    @Mapping(target = "estagioId", source = "estagio.id")
    @Mapping(target = "estagioNome", source = "estagio.nome")
    OportunidadeResponse toResponse(Oportunidade oportunidade);
}
