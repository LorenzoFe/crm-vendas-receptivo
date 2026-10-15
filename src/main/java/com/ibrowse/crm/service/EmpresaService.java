package com.ibrowse.crm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.EmpresaRequest;
import com.ibrowse.crm.dto.response.EmpresaResponse;
import com.ibrowse.crm.entity.Empresa;
import com.ibrowse.crm.mapper.EmpresaMapper;
import com.ibrowse.crm.repository.EmpresaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaService {

	private final EmpresaRepository empresaRepository;
	private final UsuarioService usuarioService;
	private final EmpresaMapper empresaMapper;

	@Transactional(readOnly = true)
	public Page<EmpresaResponse> listar(String nome, String segmento, Long responsavelId, Boolean ativo, Pageable pageable) {
		return empresaRepository
				.filtrar(textoOuVazio(nome), textoOuVazio(segmento), responsavelId, ativo, pageable)
				.map(empresaMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public EmpresaResponse buscarPorId(Long id) {
		return empresaMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public EmpresaResponse criar(EmpresaRequest request) {
		String cnpj = textoOuNulo(request.cnpj());
		if (cnpj != null && empresaRepository.existsByCnpj(cnpj)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma empresa com o CNPJ " + cnpj);
		}

		Empresa empresa = empresaMapper.toEntity(request);
		empresa.setCnpj(cnpj);
		empresa.setAtivo(true);
		empresa.setUsuarioResponsavel(usuarioService.buscarResponsavel(request.usuarioResponsavelId()));

		return empresaMapper.toResponse(empresaRepository.save(empresa));
	}

	@Transactional
	public EmpresaResponse atualizar(Long id, EmpresaRequest request) {
		Empresa empresa = buscarEntidade(id);

		String cnpj = textoOuNulo(request.cnpj());
		if (cnpj != null && empresaRepository.existsByCnpjAndIdNot(cnpj, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma empresa com o CNPJ " + cnpj);
		}

		empresaMapper.atualizar(request, empresa);
		empresa.setCnpj(cnpj);
		empresa.setUsuarioResponsavel(usuarioService.buscarResponsavel(request.usuarioResponsavelId()));

		return empresaMapper.toResponse(empresaRepository.saveAndFlush(empresa));
	}

	@Transactional
	public void desativar(Long id) {
		buscarEntidade(id).setAtivo(false);
	}

	public Empresa buscarEntidade(Long id) {
		return empresaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	public Empresa buscarEmpresaAtiva(Long id) {
		Empresa empresa = empresaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa " + id + " não encontrada"));
		if (!Boolean.TRUE.equals(empresa.getAtivo())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empresa " + id + " está inativa");
		}
		return empresa;
	}

	private String textoOuVazio(String texto) {
		return texto == null ? "" : texto.trim();
	}

	private String textoOuNulo(String texto) {
		return texto == null || texto.isBlank() ? null : texto.trim();
	}

}
