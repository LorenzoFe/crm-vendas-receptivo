package com.ibrowse.crm.mapper;

import org.mapstruct.*;
import com.ibrowse.crm.dto.request.InteracaoRequest;
import com.ibrowse.crm.dto.response.InteracaoResponse;
import com.ibrowse.crm.entity.Interacao;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface InteracaoMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "contatoId", source = "contato.id")
    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNome", source = "usuario.nome")
    InteracaoResponse toResponse(Interacao entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    Interacao toEntity(InteracaoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void atualizar(InteracaoRequest request, @MappingTarget Interacao entity);
}
