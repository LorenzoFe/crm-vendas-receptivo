package com.ibrowse.crm.service;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.dto.request.InteracaoRequest;
import com.ibrowse.crm.dto.response.InteracaoResponse;
import com.ibrowse.crm.entity.Interacao;
import com.ibrowse.crm.mapper.InteracaoMapper;
import com.ibrowse.crm.repository.InteracaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InteracaoService {

    private final InteracaoRepository repository;
    private final InteracaoMapper mapper;
    private final VinculosAcompanhamentoService vinculos;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<InteracaoResponse> listar(Pageable pageable) {
        return repository.findAll(VinculosAcompanhamentoService.pagina(pageable)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public InteracaoResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public InteracaoResponse criar(InteracaoRequest request) {
        Long autorId = vinculos.autor(request.usuarioId());
        Interacao entity = mapper.toEntity(request);
        if (entity.getDataInteracao() == null) {
            entity.setDataInteracao(LocalDateTime.now(clock));
        }
        preencherVinculos(entity, request, autorId);
        return salvar(entity);
    }

    @Transactional
    public InteracaoResponse atualizar(Long id, InteracaoRequest request) {
        Interacao entity = buscarEntidade(id);
        vinculos.validarAutoria(entity.getUsuario().getId());
        Long autorId = vinculos.autor(request.usuarioId());
        LocalDateTime dataAnterior = entity.getDataInteracao();
        mapper.atualizar(request, entity);
        if (request.dataInteracao() == null) {
            entity.setDataInteracao(dataAnterior);
        }
        preencherVinculos(entity, request, autorId);
        return salvar(entity);
    }

    @Transactional
    public void excluir(Long id) {
        Interacao entity = buscarEntidade(id);
        vinculos.validarAutoria(entity.getUsuario().getId());
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registro possui vínculos que impedem a exclusão", exception);
        }
    }

    private void preencherVinculos(Interacao entity, InteracaoRequest request, Long autorId) {
        var resolvidos = vinculos.resolver(request.empresaId(), request.contatoId(), request.oportunidadeId(), true);
        entity.setEmpresa(resolvidos.empresa());
        entity.setContato(resolvidos.contato());
        entity.setOportunidade(resolvidos.oportunidade());
        entity.setUsuario(vinculos.usuarioAtivo(autorId));
    }

    private InteracaoResponse salvar(Interacao entity) {
        try {
            return mapper.toResponse(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível salvar o registro com os vínculos informados", exception);
        }
    }

    private Interacao buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
    }
}
