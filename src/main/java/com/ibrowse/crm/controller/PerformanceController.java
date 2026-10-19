package com.ibrowse.crm.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibrowse.crm.dto.response.PerformanceVendedorResponse;
import com.ibrowse.crm.service.PerformanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Relatórios", description = "Indicadores de desempenho")
public class PerformanceController {

	private final PerformanceService performanceService;

	@GetMapping("/usuarios/{id}/performance")
	@PreAuthorize("isAuthenticated() and (hasAuthority('ver_relatorio') or #id == authentication.principal.id())")
	@Operation(summary = "Performance de um vendedor no período",
			description = "Datas no formato AAAA-MM-DD. Padrão: do dia 1 do mês atual até hoje. "
					+ "O próprio vendedor pode consultar a sua; para os outros é preciso a permissão ver_relatorio.")
	public PerformanceVendedorResponse performance(
			@PathVariable Long id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
		return performanceService.calcular(id, inicio, fim);
	}

}
