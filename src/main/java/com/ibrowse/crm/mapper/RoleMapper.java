package com.ibrowse.crm.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.ibrowse.crm.dto.response.PermissaoResponse;
import com.ibrowse.crm.dto.response.RoleResponse;
import com.ibrowse.crm.entity.Permissao;
import com.ibrowse.crm.entity.Role;

@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface RoleMapper {

	RoleResponse toResponse(Role role);

	PermissaoResponse toResponse(Permissao permissao);

	default List<PermissaoResponse> permissoesOrdenadas(Set<Permissao> permissoes) {
		return permissoes.stream()
				.sorted(Comparator.comparing(Permissao::getModulo).thenComparing(Permissao::getNome))
				.map(this::toResponse)
				.toList();
	}

}
