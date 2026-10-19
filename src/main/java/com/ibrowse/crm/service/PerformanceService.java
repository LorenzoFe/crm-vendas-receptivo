package com.ibrowse.crm.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.response.PerformanceVendedorResponse;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.entity.Usuario;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.repository.OportunidadeRepository;
import com.ibrowse.crm.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformanceService {

	private final UsuarioRepository usuarioRepository;
	private final OportunidadeRepository oportunidadeRepository;
	private final Clock clock;

	@Transactional(readOnly = true)
	public PerformanceVendedorResponse calcular(Long usuarioId, LocalDate inicio, LocalDate fim) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));

		LocalDate hoje = LocalDate.now(clock);
		LocalDate periodoInicio = inicio != null ? inicio : hoje.withDayOfMonth(1);
		LocalDate periodoFim = fim != null ? fim : hoje;
		if (periodoInicio.isAfter(periodoFim)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de início deve ser anterior ou igual à data de fim");
		}

		List<Oportunidade> oportunidades = oportunidadeRepository.findByVendedorId(usuarioId);

		long criadas = oportunidades.stream()
				.filter(o -> o.getDataCriacao() != null && dentro(o.getDataCriacao().toLocalDate(), periodoInicio, periodoFim))
				.count();

		List<Oportunidade> ativas = oportunidades.stream()
				.filter(o -> o.getStatus() == StatusOportunidade.ATIVA)
				.toList();

		List<Oportunidade> ganhas = fechadasNoPeriodo(oportunidades, StatusOportunidade.GANHA, periodoInicio, periodoFim);
		List<Oportunidade> perdidas = fechadasNoPeriodo(oportunidades, StatusOportunidade.PERDIDA, periodoInicio, periodoFim);

		BigDecimal valorEmAberto = somar(ativas.stream().map(Oportunidade::getValorEstimado).toList());
		BigDecimal totalVendido = somar(ganhas.stream().map(Oportunidade::getValorReal).toList());
		long fechadas = ganhas.size() + perdidas.size();

		BigDecimal taxaConversao = fechadas == 0 ? BigDecimal.ZERO
				: BigDecimal.valueOf(ganhas.size() * 100L).divide(BigDecimal.valueOf(fechadas), 2, RoundingMode.HALF_UP);
		BigDecimal ticketMedio = ganhas.isEmpty() ? BigDecimal.ZERO
				: totalVendido.divide(BigDecimal.valueOf(ganhas.size()), 2, RoundingMode.HALF_UP);
		BigDecimal cicloMedioDias = ganhas.isEmpty() ? BigDecimal.ZERO
				: BigDecimal.valueOf(ganhas.stream()
						.mapToLong(o -> ChronoUnit.DAYS.between(o.getDataCriacao().toLocalDate(), o.getDataFechamentoReal()))
						.average().orElse(0)).setScale(1, RoundingMode.HALF_UP);

		return new PerformanceVendedorResponse(usuario.getId(), usuario.getNome(), periodoInicio, periodoFim,
				criadas, ativas.size(), valorEmAberto, ganhas.size(), perdidas.size(),
				taxaConversao, totalVendido, ticketMedio, cicloMedioDias);
	}

	private List<Oportunidade> fechadasNoPeriodo(List<Oportunidade> oportunidades, StatusOportunidade status,
			LocalDate inicio, LocalDate fim) {
		return oportunidades.stream()
				.filter(o -> o.getStatus() == status)
				.filter(o -> o.getDataFechamentoReal() != null && dentro(o.getDataFechamentoReal(), inicio, fim))
				.toList();
	}

	private boolean dentro(LocalDate data, LocalDate inicio, LocalDate fim) {
		return !data.isBefore(inicio) && !data.isAfter(fim);
	}

	private BigDecimal somar(List<BigDecimal> valores) {
		return valores.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
