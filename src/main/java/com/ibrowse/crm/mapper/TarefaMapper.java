package com.ibrowse.crm.mapper;

import org.mapstruct.*;
import com.ibrowse.crm.dto.request.TarefaRequest;
import com.ibrowse.crm.dto.response.TarefaResponse;
import com.ibrowse.crm.entity.Tarefa;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TarefaMapper {

    @Mapping(target = "empresaId", source = "empresa.id")
    @Mapping(target = "contatoId", source = "contato.id")
    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    @Mapping(target = "responsavelId", source = "responsavel.id")
    @Mapping(target = "responsavelNome", source = "responsavel.nome")
    TarefaResponse toResponse(Tarefa entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    Tarefa toEntity(TarefaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "contato", ignore = true)
    @Mapping(target = "oportunidade", ignore = true)
    @Mapping(target = "responsavel", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    @Mapping(target = "dataConclusao", ignore = true)
    void atualizar(TarefaRequest request, @MappingTarget Tarefa entity);
}
