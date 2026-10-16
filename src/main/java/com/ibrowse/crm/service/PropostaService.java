package com.ibrowse.crm.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.response.PropostaResponse;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.entity.Proposta;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.enums.StatusProposta;
import com.ibrowse.crm.mapper.PropostaMapper;
import com.ibrowse.crm.repository.PropostaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropostaService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("9999999999999.99");
    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "numero", "status", "valorTotal",
            "dataEmissao", "dataVencimento", "dataAceitacao", "dataCriacao", "dataAtualizacao");
    private final PropostaRepository propostaRepository;
    private final PropostaMapper propostaMapper;
    private final NumeroPropostaService numeroPropostaService;
    private final EntityManager entityManager;
    private final Clock clock;

    @Transactional
    public PropostaResponse gerarDeOportunidade(Long oportunidadeId) {
        Oportunidade oportunidade = bloquearOportunidade(oportunidadeId);
        exigirOportunidadeAtiva(oportunidade);
        BigDecimal total = somarSubtotais(oportunidadeId);
        LocalDate hoje = LocalDate.now(clock);
        Proposta proposta = new Proposta();
        proposta.setNumero(numeroPropostaService.proximo(hoje));
        proposta.setOportunidade(oportunidade);
        proposta.setValorTotal(total);
        proposta.setStatus(StatusProposta.RASCUNHO);
        proposta.setDataEmissao(hoje);
        proposta.setDataVencimento(hoje.plusDays(15));
        return salvar(proposta);
    }

    @Transactional
    public PropostaResponse enviar(Long id) {
        Proposta proposta = bloquearProposta(id);
        exigirStatus(proposta, StatusProposta.RASCUNHO);
        proposta.setStatus(StatusProposta.ENVIADA);
        return salvar(proposta);
    }

    @Transactional
    public PropostaResponse aceitar(Long id) {
        Proposta proposta = bloquearProposta(id);
        exigirStatus(proposta, StatusProposta.ENVIADA);
        Oportunidade oportunidade = proposta.getOportunidade();
        exigirOportunidadeAtiva(oportunidade);
        LocalDate hoje = LocalDate.now(clock);
        proposta.setStatus(StatusProposta.ACEITA);
        proposta.setDataAceitacao(hoje);
        oportunidade.setStatus(StatusOportunidade.GANHA);
        oportunidade.setValorReal(proposta.getValorTotal());
        oportunidade.setDataFechamentoReal(hoje);
        return salvar(proposta);
    }

    @Transactional
    public PropostaResponse rejeitar(Long id) {
        Proposta proposta = bloquearProposta(id);
        exigirStatus(proposta, StatusProposta.ENVIADA);
        proposta.setStatus(StatusProposta.REJEITADA);
        return salvar(proposta);
    }

    @Transactional(readOnly = true)
    public PropostaResponse buscarPorId(Long id) {
        validarId(id);
        return propostaMapper.toResponse(propostaRepository.findById(id)
                .orElseThrow(() -> naoEncontrada("Proposta", id)));
    }

    @Transactional(readOnly = true)
    public Page<PropostaResponse> listar(StatusProposta status, Long oportunidadeId, Pageable pageable) {
        if (oportunidadeId != null) {
            validarId(oportunidadeId);
        }
        validarPaginacao(pageable);
        return propostaRepository.filtrar(status, oportunidadeId, pageable).map(propostaMapper::toResponse);
    }

    private BigDecimal somarSubtotais(Long oportunidadeId) {
        List<BigDecimal> subtotais = entityManager.createQuery(
                "select i.subtotal from ItemOportunidade i where i.oportunidade.id = :oportunidadeId",
                BigDecimal.class).setParameter("oportunidadeId", oportunidadeId).getResultList();
        if (subtotais.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "A oportunidade precisa ter pelo menos um item para gerar uma proposta");
        }
        if (subtotais.stream().anyMatch(valor -> valor == null || valor.signum() < 0)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "A oportunidade contém subtotal inválido");
        }
        BigDecimal total = subtotais.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(VALOR_MAXIMO) > 0) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "O valor total ultrapassa o limite permitido para uma proposta");
        }
        return total;
    }

    private Proposta bloquearProposta(Long id) {
        validarId(id);
        Long oportunidadeId = propostaRepository.buscarOportunidadeId(id)
                .orElseThrow(() -> naoEncontrada("Proposta", id));
        bloquearOportunidade(oportunidadeId);
        Proposta proposta = entityManager.find(Proposta.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (proposta == null) {
            throw naoEncontrada("Proposta", id);
        }
        return proposta;
    }

    private Oportunidade bloquearOportunidade(Long id) {
        validarId(id);
        Oportunidade oportunidade = entityManager.find(Oportunidade.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (oportunidade == null) {
            throw naoEncontrada("Oportunidade", id);
        }
        return oportunidade;
    }

    private void exigirOportunidadeAtiva(Oportunidade oportunidade) {
        if (oportunidade.getStatus() != StatusOportunidade.ATIVA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A oportunidade deve estar ATIVA");
        }
    }

    private void exigirStatus(Proposta proposta, StatusProposta esperado) {
        if (proposta.getStatus() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A proposta deve estar " + esperado + " para executar esta ação");
        }
    }

    private PropostaResponse salvar(Proposta proposta) {
        try {
            return propostaMapper.toResponse(propostaRepository.saveAndFlush(proposta));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não foi possível salvar a proposta. Verifique os dados e tente novamente.", exception);
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O id deve ser positivo");
        }
    }

    private void validarPaginacao(Pageable pageable) {
        if (pageable.isUnpaged() || pageable.getPageSize() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe uma página com até 100 propostas");
        }
        if (pageable.getSort().stream().anyMatch(ordem -> !CAMPOS_ORDENACAO.contains(ordem.getProperty()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação de propostas inválido");
        }
    }

    private ResponseStatusException naoEncontrada(String recurso, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, recurso + " " + id + " não encontrada");
    }
}
