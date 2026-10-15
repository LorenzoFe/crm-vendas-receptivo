package com.ibrowse.crm.dto.response;

import java.util.List;

public record RoleResponse(
		Long id,
		String nome,
		String descricao,
		List<PermissaoResponse> permissoes) {
}
