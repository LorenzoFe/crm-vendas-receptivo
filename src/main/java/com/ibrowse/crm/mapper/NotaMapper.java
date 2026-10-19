package com.ibrowse.crm.mapper;

import org.mapstruct.*;
import com.ibrowse.crm.dto.request.NotaRequest;
import com.ibrowse.crm.dto.response.NotaResponse;
import com.ibrowse.crm.entity.Nota;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface NotaMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "contatoId", source = "contato.id")
    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNome", source = "usuario.nome")
    NotaResponse toResponse(Nota entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Nota toEntity(NotaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    void atualizar(NotaRequest request, @MappingTarget Nota entity);
}
