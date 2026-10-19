package com.ibrowse.crm.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.ibrowse.crm.enums.StatusTarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.dto.request.TarefaRequest;
import com.ibrowse.crm.dto.response.TarefaResponse;
import com.ibrowse.crm.entity.Tarefa;
import com.ibrowse.crm.mapper.TarefaMapper;
import com.ibrowse.crm.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository repository;
    private final TarefaMapper mapper;
    private final VinculosAcompanhamentoService vinculos;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Page<TarefaResponse> listar(Pageable pageable) {
        return repository.findAll(VinculosAcompanhamentoService.pagina(pageable)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public TarefaResponse criar(TarefaRequest request) {
        Tarefa entity = mapper.toEntity(request);
        if (entity.getStatus() == null) {
            entity.setStatus(StatusTarefa.PENDENTE);
        }
        preencherVinculos(entity, request);
        ajustarConclusao(entity);
        return salvar(entity);
    }

    @Transactional
    public TarefaResponse atualizar(Long id, TarefaRequest request) {
        Tarefa entity = buscarEntidade(id);
        StatusTarefa statusAnterior = entity.getStatus();
        mapper.atualizar(request, entity);
        if (request.status() == null) {
            entity.setStatus(statusAnterior);
        }
        preencherVinculos(entity, request);
        ajustarConclusao(entity);
        return salvar(entity);
    }

    @Transactional
    public void excluir(Long id) {
        Tarefa entity = buscarEntidade(id);
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registro possui vínculos que impedem a exclusão", exception);
        }
    }

    @Transactional(readOnly = true)
    public Page<TarefaResponse> urgentes(int dias, Pageable pageable) {
        if (dias < 0 || dias > 365) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dias deve estar entre 0 e 365");
        }
        Pageable validada = VinculosAcompanhamentoService.pagina(pageable);
        Pageable pagina = org.springframework.data.domain.PageRequest.of(validada.getPageNumber(), validada.getPageSize());
        return repository.buscarUrgentes(LocalDate.now(clock).plusDays(dias), pagina).map(mapper::toResponse);
    }

    @Transactional
    public TarefaResponse concluir(Long id) {
        Tarefa tarefa = buscarEntidade(id);
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(LocalDateTime.now(clock));
        return salvar(tarefa);
    }

    private void ajustarConclusao(Tarefa tarefa) {
        if (tarefa.getStatus() != StatusTarefa.CONCLUIDA) {
            tarefa.setDataConclusao(null);
            return;
        }
        if (tarefa.getDataConclusao() == null) {
            tarefa.setDataConclusao(LocalDateTime.now(clock));
        }
    }

    private void preencherVinculos(Tarefa entity, TarefaRequest request) {
        var resolvidos = vinculos.resolver(request.empresaId(), request.contatoId(), request.oportunidadeId(), false);
        entity.setEmpresa(resolvidos.empresa());
        entity.setContato(resolvidos.contato());
        entity.setOportunidade(resolvidos.oportunidade());
        entity.setResponsavel(vinculos.usuarioAtivo(request.responsavelId()));
    }

    private TarefaResponse salvar(Tarefa entity) {
        try {
            return mapper.toResponse(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível salvar o registro com os vínculos informados", exception);
        }
    }

    private Tarefa buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
    }
}
