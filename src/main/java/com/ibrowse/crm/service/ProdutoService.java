package com.ibrowse.crm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.ProdutoRequest;
import com.ibrowse.crm.dto.response.ProdutoResponse;
import com.ibrowse.crm.entity.Produto;
import com.ibrowse.crm.mapper.ProdutoMapper;
import com.ibrowse.crm.repository.ProdutoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

	private final ProdutoRepository produtoRepository;
	private final ProdutoMapper produtoMapper;

	@Transactional(readOnly = true)
	public Page<ProdutoResponse> listar(String nome, Boolean ativo, Pageable pageable) {
		String filtroNome = nome == null ? "" : nome.trim();
		return produtoRepository
				.findByNomeContainingIgnoreCaseAndAtivo(filtroNome, ativo, pageable)
				.map(produtoMapper::toResponse);
	}

	@Transactional(readOnly = true)
	public ProdutoResponse buscarPorId(Long id) {
		return produtoMapper.toResponse(buscarEntidade(id));
	}

	@Transactional
	public ProdutoResponse criar(ProdutoRequest request) {
		Produto produto = produtoMapper.toEntity(request);
		produto.setAtivo(true);
		return produtoMapper.toResponse(produtoRepository.save(produto));
	}

	@Transactional
	public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
		Produto produto = buscarEntidade(id);
		produtoMapper.atualizar(request, produto);
		return produtoMapper.toResponse(produtoRepository.saveAndFlush(produto));
	}

	@Transactional
	public void desativar(Long id) {
		Produto produto = buscarEntidade(id);
		produto.setAtivo(false);
	}

	private Produto buscarEntidade(Long id) {
		return produtoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto " + id + " não encontrado"));
	}

}
