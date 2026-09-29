package com.ibrowse.crm.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ibrowse.crm.dto.request.ProdutoRequest;
import com.ibrowse.crm.dto.response.ProdutoResponse;
import com.ibrowse.crm.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Catálogo de produtos e serviços vendidos")
public class ProdutoController {

	private final ProdutoService produtoService;

	@GetMapping
	@Operation(summary = "Lista produtos (paginado)", description = "Filtros opcionais: nome (parte do nome) e ativo (padrão: true).")
	public PagedModel<ProdutoResponse> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(defaultValue = "true") Boolean ativo,
			@PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
		return new PagedModel<>(produtoService.listar(nome, ativo, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um produto pelo id")
	public ProdutoResponse buscarPorId(@PathVariable Long id) {
		return produtoService.buscarPorId(id);
	}

	@PostMapping
	@Operation(summary = "Cadastra um produto")
	public ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid ProdutoRequest request) {
		ProdutoResponse criado = produtoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza um produto")
	public ProdutoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoRequest request) {
		return produtoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Desativa um produto (exclusão lógica)")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		produtoService.desativar(id);
		return ResponseEntity.noContent().build();
	}

}
