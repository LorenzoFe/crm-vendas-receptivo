package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.response.ItemOportunidadeResponse;
import com.ibrowse.crm.entity.ItemOportunidade;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ItemOportunidadeMapper {

    @Mapping(target = "oportunidadeId", source = "oportunidade.id")
    @Mapping(target = "produtoId", source = "produto.id")
    @Mapping(target = "produtoNome", source = "produto.nome")
    ItemOportunidadeResponse toResponse(ItemOportunidade item);
}
