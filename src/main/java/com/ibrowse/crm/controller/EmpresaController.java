package com.ibrowse.crm.controller;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.ibrowse.crm.dto.request.EmpresaRequest;
import com.ibrowse.crm.dto.response.ContatoResponse;
import com.ibrowse.crm.dto.response.EmpresaResponse;
import com.ibrowse.crm.service.ContatoService;
import com.ibrowse.crm.service.EmpresaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Empresas clientes")
public class EmpresaController {

	private final EmpresaService empresaService;
	private final ContatoService contatoService;

	@GetMapping
	@Operation(summary = "Lista empresas (paginado)", description = "Filtros opcionais: nome, segmento, responsavelId e ativo (padrão: true).")
	public PagedModel<EmpresaResponse> listar(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String segmento,
			@RequestParam(required = false) Long responsavelId,
			@RequestParam(defaultValue = "true") Boolean ativo,
			@PageableDefault(size = 20, sort = "nomeRazaoSocial", direction = Sort.Direction.ASC) Pageable pageable) {
		return new PagedModel<>(empresaService.listar(nome, segmento, responsavelId, ativo, pageable));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Busca uma empresa pelo id")
	public EmpresaResponse buscarPorId(@PathVariable Long id) {
		return empresaService.buscarPorId(id);
	}

	@GetMapping("/{id}/contatos")
	@Operation(summary = "Lista os contatos de uma empresa")
	public PagedModel<ContatoResponse> listarContatos(
			@PathVariable Long id,
			@RequestParam(defaultValue = "true") Boolean ativo,
			@PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
		return new PagedModel<>(contatoService.listar(null, id, ativo, pageable));
	}

	@PostMapping
	@PreAuthorize("hasAuthority('criar_empresa')")
	@Operation(summary = "Cadastra uma empresa")
	public ResponseEntity<EmpresaResponse> criar(@RequestBody @Valid EmpresaRequest request) {
		EmpresaResponse criada = empresaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}").buildAndExpand(criada.id()).toUri();
		return ResponseEntity.created(location).body(criada);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAuthority('editar_empresa')")
	@Operation(summary = "Atualiza uma empresa")
	public EmpresaResponse atualizar(@PathVariable Long id, @RequestBody @Valid EmpresaRequest request) {
		return empresaService.atualizar(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAuthority('deletar_empresa')")
	@Operation(summary = "Desativa uma empresa (exclusão lógica)")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		empresaService.desativar(id);
		return ResponseEntity.noContent().build();
	}

}
