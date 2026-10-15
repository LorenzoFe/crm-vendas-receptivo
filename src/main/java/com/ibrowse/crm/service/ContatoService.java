package com.ibrowse.crm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.ContatoRequest;
import com.ibrowse.crm.dto.response.ContatoResponse;
import com.ibrowse.crm.entity.Contato;
import com.ibrowse.crm.mapper.ContatoMapper;
import com.ibrowse.crm.repository.ContatoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContatoService {

	private final ContatoRepository contatoRepository;
	private final EmpresaService empresaService;
	private final UsuarioService usuarioService;
	private final ContatoMapper contatoMapper;

	@Transactional(readOnly = true)
	public Page<ContatoResponse> listar(String nome, Long empresaId, Boolean ativo, Pageable pageable) {
		if (empresaId != null) {
			empresaService.buscarEntidade(empresaId);
		}
		String filtroNome = nome == null ? "" : nome.trim();
		return contatoRepository.filtrar(filtroNome, empresaId, ativo, pageable).map(contatoMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public ContatoResponse buscarPorId(Long id) {
		return contatoMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public ContatoResponse criar(ContatoRequest request) {
		Contato contato = contatoMapper.toEntity(request);
		contato.setAtivo(true);
		contato.setEmpresa(empresaService.buscarEmpresaAtiva(request.empresaId()));
		contato.setUsuarioResponsavel(usuarioService.buscarResponsavel(request.usuarioResponsavelId()));
		return contatoMapper.toResponse(contatoRepository.save(contato));
	}

	@Transactional
	public ContatoResponse atualizar(Long id, ContatoRequest request) {
		Contato contato = buscarEntidade(id);
		contatoMapper.atualizar(request, contato);
		contato.setEmpresa(empresaService.buscarEmpresaAtiva(request.empresaId()));
		contato.setUsuarioResponsavel(usuarioService.buscarResponsavel(request.usuarioResponsavelId()));
		return contatoMapper.toResponse(contatoRepository.saveAndFlush(contato));
	}

	@Transactional
	public void desativar(Long id) {
		buscarEntidade(id).setAtivo(false);
	}

	public Contato buscarEntidade(Long id) {
		return contatoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contato " + id + " não encontrado"));
	}

}
