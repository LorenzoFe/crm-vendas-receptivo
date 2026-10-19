package com.ibrowse.crm.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.MudarEstagioRequest;
import com.ibrowse.crm.dto.request.OportunidadeRequest;
import com.ibrowse.crm.dto.request.PerderOportunidadeRequest;
import com.ibrowse.crm.dto.response.OportunidadeResponse;
import com.ibrowse.crm.entity.Empresa;
import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.mapper.OportunidadeMapper;
import com.ibrowse.crm.repository.OportunidadeRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OportunidadeService {

    private final EntityManager entityManager;
    private final ReferenciasVenda referencias;
    private final OportunidadeMapper mapper;
    private final OportunidadeRepository repository;
    private final Clock clock;

    @Transactional
    public OportunidadeResponse criar(OportunidadeRequest request) {
        Oportunidade oportunidade = new Oportunidade();
        preencher(oportunidade, request);
        oportunidade.setStatus(StatusOportunidade.ATIVA);
        entityManager.persist(oportunidade);
        entityManager.flush();
        return mapper.toResponse(oportunidade);
    }

    @Transactional
    public OportunidadeResponse atualizar(Long id, OportunidadeRequest request) {
        Oportunidade oportunidade = bloquear(id);
        exigirAtiva(oportunidade);
        validarTrocaDeEmpresa(oportunidade, request.empresaId());
        preencher(oportunidade, request);
        entityManager.flush();
        return mapper.toResponse(oportunidade);
    }

    @Transactional
    public OportunidadeResponse mudarEstagio(Long id, MudarEstagioRequest request) {
        Oportunidade oportunidade = bloquear(id);
        exigirAtiva(oportunidade);
        oportunidade.setEstagio(referencias.estagio(request.estagioId()));
        entityManager.flush();
        return mapper.toResponse(oportunidade);
    }

    @Transactional
    public OportunidadeResponse perder(Long id, PerderOportunidadeRequest request) {
        Oportunidade oportunidade = bloquear(id);
        exigirAtiva(oportunidade);
        oportunidade.setStatus(StatusOportunidade.PERDIDA);
        oportunidade.setMotivoPerda(request.motivo());
        oportunidade.setDataFechamentoReal(LocalDate.now(clock));
        entityManager.flush();
        return mapper.toResponse(oportunidade);
    }

    @Transactional
    public void excluir(Long id) {
        Oportunidade oportunidade = bloquear(id);
        exigirAtiva(oportunidade);
        if (repository.possuiDependencias(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A oportunidade possui itens, propostas ou registros de acompanhamento");
        }
        entityManager.remove(oportunidade);
        entityManager.flush();
    }

    private void validarTrocaDeEmpresa(Oportunidade oportunidade, Long empresaId) {
        if (!oportunidade.getEmpresa().getId().equals(empresaId)
                && repository.possuiDependencias(oportunidade.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível trocar a empresa de uma oportunidade com registros vinculados");
        }
    }

    private void preencher(Oportunidade oportunidade, OportunidadeRequest request) {
        Empresa empresa = referencias.empresa(request.empresaId());
        oportunidade.setTitulo(request.titulo());
        oportunidade.setDescricao(request.descricao());
        oportunidade.setEmpresa(empresa);
        oportunidade.setContato(referencias.contato(request.contatoId(), empresa));
        oportunidade.setVendedor(referencias.vendedor(request.vendedorId()));
        oportunidade.setEstagio(request.estagioId() == null && oportunidade.getId() != null
                ? oportunidade.getEstagio() : referencias.estagio(request.estagioId()));
        oportunidade.setValorEstimado(request.valorEstimado());
        oportunidade.setDataFechamentoEsperada(request.dataFechamentoEsperada());
    }

    private Oportunidade bloquear(Long id) {
        Oportunidade oportunidade = entityManager.find(Oportunidade.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (oportunidade == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade " + id + " não encontrada");
        }
        return oportunidade;
    }

    private void exigirAtiva(Oportunidade oportunidade) {
        if (oportunidade.getStatus() != StatusOportunidade.ATIVA) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Esta operação exige uma oportunidade ATIVA");
        }
    }
}
