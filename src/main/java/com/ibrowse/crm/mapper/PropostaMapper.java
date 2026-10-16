package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.response.PropostaResponse;
import com.ibrowse.crm.entity.Proposta;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PropostaMapper {

    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    PropostaResponse toResponse(Proposta proposta);
}
