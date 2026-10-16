package com.ibrowse.crm.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.dto.request.NotaRequest;
import com.ibrowse.crm.dto.response.NotaResponse;
import com.ibrowse.crm.entity.Nota;
import com.ibrowse.crm.mapper.NotaMapper;
import com.ibrowse.crm.repository.NotaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotaService {

    private final NotaRepository repository;
    private final NotaMapper mapper;
    private final VinculosAcompanhamentoService vinculos;

    @Transactional(readOnly = true)
    public Page<NotaResponse> listar(Pageable pageable) {
        return repository.findAll(VinculosAcompanhamentoService.pagina(pageable)).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NotaResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public NotaResponse criar(NotaRequest request) {
        vinculos.validarAutoria(request.usuarioId());
        Nota entity = mapper.toEntity(request);
        preencherVinculos(entity, request);
        return salvar(entity);
    }

    @Transactional
    public NotaResponse atualizar(Long id, NotaRequest request) {
        Nota entity = buscarEntidade(id);
        vinculos.validarAutoria(request.usuarioId());
        vinculos.validarAutoria(entity.getUsuario().getId());
        mapper.atualizar(request, entity);
        preencherVinculos(entity, request);
        return salvar(entity);
    }

    @Transactional
    public void excluir(Long id) {
        Nota entity = buscarEntidade(id);
        vinculos.validarAutoria(entity.getUsuario().getId());
        try {
            repository.delete(entity);
            repository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Registro possui vínculos que impedem a exclusão", exception);
        }
    }

    private void preencherVinculos(Nota entity, NotaRequest request) {
        var resolvidos = vinculos.resolver(request.empresaId(), request.contatoId(), request.oportunidadeId(), true);
        entity.setEmpresa(resolvidos.empresa());
        entity.setContato(resolvidos.contato());
        entity.setOportunidade(resolvidos.oportunidade());
        entity.setUsuario(vinculos.usuarioAtivo(request.usuarioId()));
    }

    private NotaResponse salvar(Nota entity) {
        try {
            return mapper.toResponse(repository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível salvar o registro com os vínculos informados", exception);
        }
    }

    private Nota buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro não encontrado"));
    }
}
