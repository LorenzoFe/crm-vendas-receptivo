package com.ibrowse.crm.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.EstagioVendaRequest;
import com.ibrowse.crm.dto.response.EstagioVendaResponse;
import com.ibrowse.crm.entity.EstagioVenda;
import com.ibrowse.crm.mapper.EstagioVendaMapper;
import com.ibrowse.crm.repository.EstagioVendaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstagioVendaService {

    private final EstagioVendaRepository estagioVendaRepository;
    private final EstagioVendaMapper estagioVendaMapper;

    @Transactional(readOnly = true)
    public List<EstagioVendaResponse> listar() {
        return estagioVendaRepository.findAllByOrderByOrdemAscIdAsc().stream()
                .map(estagioVendaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstagioVendaResponse buscarPorId(Long id) {
        return estagioVendaMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public EstagioVendaResponse criar(EstagioVendaRequest request) {
        validarNomeDisponivel(request.nome(), null);
        return salvar(estagioVendaMapper.toEntity(request));
    }

    @Transactional
    public EstagioVendaResponse atualizar(Long id, EstagioVendaRequest request) {
        EstagioVenda estagio = buscarEntidade(id);
        validarNomeDisponivel(request.nome(), id);
        estagioVendaMapper.atualizar(request, estagio);
        return salvar(estagio);
    }

    @Transactional
    public void excluir(Long id) {
        EstagioVenda estagio = buscarEntidade(id);
        if (estagioVendaRepository.existeOportunidadeVinculada(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir um estágio utilizado em oportunidades");
        }
        excluirDoBanco(estagio);
    }

    private void excluirDoBanco(EstagioVenda estagio) {
        try {
            estagioVendaRepository.delete(estagio);
            estagioVendaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não foi possível excluir o estágio porque existem registros vinculados", exception);
        }
    }

    private EstagioVendaResponse salvar(EstagioVenda estagio) {
        try {
            return estagioVendaMapper.toResponse(estagioVendaRepository.saveAndFlush(estagio));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não foi possível salvar o estágio. Verifique nome duplicado ou dados incompatíveis.", exception);
        }
    }

    private EstagioVenda buscarEntidade(Long id) {
        return estagioVendaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estágio " + id + " não encontrado"));
    }

    private void validarNomeDisponivel(String nome, Long id) {
        boolean nomeOcupado = id == null
                ? estagioVendaRepository.existsByNome(nome)
                : estagioVendaRepository.existsByNomeAndIdNot(nome, id);
        if (nomeOcupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um estágio com esse nome");
        }
    }
}
