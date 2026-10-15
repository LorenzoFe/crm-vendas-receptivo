package com.ibrowse.crm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.request.ContatoRequest;
import com.ibrowse.crm.dto.response.ContatoResponse;
import com.ibrowse.crm.entity.Contato;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ContatoMapper {

	@Mapping(target = "empresaId", source = "empresa.id")
	@Mapping(target = "empresaNome", source = "empresa.nomeRazaoSocial")
	@Mapping(target = "usuarioResponsavelId", source = "usuarioResponsavel.id")
	@Mapping(target = "usuarioResponsavelNome", source = "usuarioResponsavel.nome")
	ContatoResponse toResponse(Contato contato);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "empresa", ignore = true)
	@Mapping(target = "usuarioResponsavel", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	Contato toEntity(ContatoRequest request);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "ativo", ignore = true)
	@Mapping(target = "empresa", ignore = true)
	@Mapping(target = "usuarioResponsavel", ignore = true)
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataAtualizacao", ignore = true)
	void atualizar(ContatoRequest request, @MappingTarget Contato contato);

}
