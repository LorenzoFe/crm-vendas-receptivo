package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.request.EmpresaRequest;
import com.ibrowse.crm.dto.response.EmpresaResponse;
import com.ibrowse.crm.entity.Empresa;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface EmpresaMapper {

	@Mapping(target = "usuarioResponsavelId", source = "usuarioResponsavel.id")
	@Mapping(target = "usuarioResponsavelNome", source = "usuarioResponsavel.nome")
	EmpresaResponse toResponse(Empresa empresa);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "usuarioResponsavel", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	Empresa toEntity(EmpresaRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "usuarioResponsavel", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	void atualizar(EmpresaRequest request, @MappingTarget Empresa empresa);

}
