package com.ibrowse.crm.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibrowse.crm.dto.response.PermissaoResponse;
import com.ibrowse.crm.dto.response.RoleResponse;
import com.ibrowse.crm.mapper.RoleMapper;
import com.ibrowse.crm.repository.PermissaoRepository;
import com.ibrowse.crm.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {

	private final RoleRepository roleRepository;
	private final PermissaoRepository permissaoRepository;
	private final RoleMapper roleMapper;

	@Transactional(readOnly = true)
	public List<RoleResponse> listarRoles() {
		return roleRepository.findAllByOrderByNomeAsc().stream().map(roleMapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<PermissaoResponse> listarPermissoes() {
		return permissaoRepository.findAllByOrderByModuloAscNomeAsc().stream().map(roleMapper::toResponse).toList();
	}

}
