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

import com.ibrowse.crm.dto.request.ContatoRequest;
import com.ibrowse.crm.dto.response.ContatoResponse;
import com.ibrowse.crm.service.ContatoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/contatos")
@RequiredArgsConstructor
@Tag(name = "Contatos", description = "Pessoas das empresas clientes")
public class ContatoController {

	private final ContatoService contatoService;

	@GetMapping
	@Operation(summary = "Lista contatos (paginado)", description = "Filtros opcionais: nome, empresaId e ativo (padrão: true).")
	public PagedModel<ContatoResponse> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) Long empresaId,
			@RequestParam(defaultValue = "true") Boolean ativo,
			@PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
		return new PagedModel<>(contatoService.listar(nome, empresaId, ativo, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca um contato pelo id")
	public ContatoResponse buscarPorId(@PathVariable Long id) {
		return contatoService.buscarPorId(id);
	}

	@PostMapping
	@Operation(summary = "Cadastra um contato")
	public ResponseEntity<ContatoResponse> criar(@RequestBody @Valid ContatoRequest request) {
		ContatoResponse criado = contatoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(criado.id()).toUri();
		return ResponseEntity.created(location).body(criado);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Atualiza um contato")
	public ContatoResponse atualizar(@PathVariable Long id, @RequestBody @Valid ContatoRequest request) {
		return contatoService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Desativa um contato (exclusão lógica)")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		contatoService.desativar(id);
		return ResponseEntity.noContent().build();
	}

}
