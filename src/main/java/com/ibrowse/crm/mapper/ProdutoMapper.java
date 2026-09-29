package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.request.ProdutoRequest;
import com.ibrowse.crm.dto.response.ProdutoResponse;
import com.ibrowse.crm.entity.Produto;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ProdutoMapper {

	ProdutoResponse toResponse(Produto produto);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	Produto toEntity(ProdutoRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	void atualizar(ProdutoRequest request, @MappingTarget Produto produto);

}
