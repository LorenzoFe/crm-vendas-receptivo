package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.request.EstagioVendaRequest;
import com.ibrowse.crm.entity.EstagioVenda;
import com.ibrowse.crm.mapper.EstagioVendaMapper;
import com.ibrowse.crm.repository.EstagioVendaRepository;

@ExtendWith(MockitoExtension.class)
class EstagioVendaServiceTest {

    @Mock
    private EstagioVendaRepository repository;
    private EstagioVendaMapper mapper;
    private EstagioVendaService service;

    @BeforeEach
    void preparar() {
        mapper = Mappers.getMapper(EstagioVendaMapper.class);
        service = new EstagioVendaService(repository, mapper);
    }

    @Test
    void criarRetornaDadosPersistidos() {
        when(repository.saveAndFlush(any(EstagioVenda.class))).thenAnswer(invocacao -> {
            EstagioVenda estagio = invocacao.getArgument(0);
            estagio.setId(7L);
            estagio.setDataCriacao(LocalDateTime.of(2026, 10, 16, 10, 0));
            return estagio;
        });

        var response = service.criar(new EstagioVendaRequest(" Qualificação ", 2, 30));

        assertEquals(7L, response.id());
        assertEquals("Qualificação", response.nome());
        assertEquals(2, response.ordem());
        assertEquals(30, response.probabilidadeConversao());
        assertNotNull(response.dataCriacao());
    }

    @Test
    void nomeDuplicadoImpedeCadastro() {
        when(repository.existsByNome("Proposta")).thenReturn(true);

        var erro = assertThrows(ResponseStatusException.class,
                () -> service.criar(new EstagioVendaRequest("Proposta", 3, 50)));

        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void conflitoNoBancoTambemRetorna409() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("simulado"));

        var erro = assertThrows(ResponseStatusException.class,
                () -> service.criar(new EstagioVendaRequest("Proposta", 3, 50)));

        assertEquals(409, erro.getStatusCode().value());
    }

    @Test
    void buscaInexistenteRetorna404() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.buscarPorId(99L));

        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void atualizarMantendoNomePreservaIdentidadeEData() {
        EstagioVenda existente = estagio(3L, "Proposta", 3);
        LocalDateTime dataOriginal = LocalDateTime.of(2026, 1, 1, 8, 0);
        existente.setDataCriacao(dataOriginal);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));
        when(repository.saveAndFlush(existente)).thenReturn(existente);

        var response = service.atualizar(3L, new EstagioVendaRequest("Proposta", 4, 75));

        assertEquals(3L, response.id());
        assertEquals(dataOriginal, response.dataCriacao());
        assertEquals(4, response.ordem());
        assertEquals(75, response.probabilidadeConversao());
        verify(repository).existsByNomeAndIdNot("Proposta", 3L);
    }

    @Test
    void editarParaNomeDeOutroEstagioRetorna409() {
        when(repository.findById(3L)).thenReturn(Optional.of(estagio(3L, "Proposta", 3)));
        when(repository.existsByNomeAndIdNot("Fechado", 3L)).thenReturn(true);

        var erro = assertThrows(ResponseStatusException.class,
                () -> service.atualizar(3L, new EstagioVendaRequest("Fechado", 5, 100)));

        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void listarUsaConsultaOrdenadaEConverteParaDto() {
        when(repository.findAllByOrderByOrdemAscIdAsc()).thenReturn(
                List.of(estagio(2L, "Prospecção", 1), estagio(1L, "Proposta", 3)));

        var response = service.listar();

        assertEquals(List.of("Prospecção", "Proposta"),
                response.stream().map(item -> item.nome()).toList());
    }

    @Test
    void excluirEstagioEmUsoRetorna409SemApagar() {
        when(repository.findById(3L)).thenReturn(Optional.of(estagio(3L, "Proposta", 3)));
        when(repository.existeOportunidadeVinculada(3L)).thenReturn(true);

        var erro = assertThrows(ResponseStatusException.class, () -> service.excluir(3L));

        assertEquals(409, erro.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    @Test
    void excluirEstagioLivreRemoveEExecutaFlush() {
        EstagioVenda existente = estagio(3L, "Proposta", 3);
        when(repository.findById(3L)).thenReturn(Optional.of(existente));

        service.excluir(3L);

        verify(repository).delete(existente);
        verify(repository).flush();
    }

    @Test
    void vinculoCriadoDuranteExclusaoTambemRetorna409() {
        when(repository.findById(3L)).thenReturn(Optional.of(estagio(3L, "Proposta", 3)));
        doThrow(new DataIntegrityViolationException("simulado")).when(repository).flush();

        var erro = assertThrows(ResponseStatusException.class, () -> service.excluir(3L));

        assertEquals(409, erro.getStatusCode().value());
    }

    @Test
    void excluirInexistenteRetorna404SemApagar() {
        var erro = assertThrows(ResponseStatusException.class, () -> service.excluir(99L));

        assertEquals(404, erro.getStatusCode().value());
        verify(repository, never()).delete(any());
    }

    private EstagioVenda estagio(Long id, String nome, int ordem) {
        EstagioVenda estagio = new EstagioVenda();
        estagio.setId(id);
        estagio.setNome(nome);
        estagio.setOrdem(ordem);
        estagio.setProbabilidadeConversao(50);
        return estagio;
    }
}
