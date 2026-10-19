package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.request.EstagioVendaRequest;
import com.ibrowse.crm.dto.response.EstagioVendaResponse;
import com.ibrowse.crm.entity.EstagioVenda;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EstagioVendaMapper {

    EstagioVendaResponse toResponse(EstagioVenda estagio);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    EstagioVenda toEntity(EstagioVendaRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "dataCriacao", ignore = true)
    void atualizar(EstagioVendaRequest request, @MappingTarget EstagioVenda estagio);
}
