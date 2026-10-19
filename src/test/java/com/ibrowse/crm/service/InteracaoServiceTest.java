package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.ibrowse.crm.dto.request.InteracaoRequest;
import com.ibrowse.crm.entity.*;
import com.ibrowse.crm.enums.TipoInteracao;
import com.ibrowse.crm.mapper.InteracaoMapper;
import com.ibrowse.crm.repository.InteracaoRepository;

@ExtendWith(MockitoExtension.class)
class InteracaoServiceTest {

    @Mock InteracaoRepository repository;
    @Mock VinculosAcompanhamentoService vinculos;
    InteracaoService service;

    @BeforeEach
    void preparar() {
        service = new InteracaoService(repository, Mappers.getMapper(InteracaoMapper.class), vinculos,
                Clock.fixed(Instant.parse("2026-10-16T13:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void criarSemDataESemUsuarioUsaAgoraEUsuarioDoToken() {
        prepararVinculos();
        when(vinculos.autor(null)).thenReturn(3L);
        when(repository.saveAndFlush(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        var resultado = service.criar(new InteracaoRequest(TipoInteracao.CHAMADA, "Ligação com cliente", null, null, null, null, 8L));

        assertEquals(3L, resultado.usuarioId());
        assertEquals(LocalDateTime.of(2026, 10, 16, 13, 0), resultado.dataInteracao());
    }

    @Test
    void criarResolveAutorEVinculosERetornaSomenteDto() {
        prepararVinculos();
        when(repository.saveAndFlush(any())).thenAnswer(invocacao -> {
            Interacao entity = invocacao.getArgument(0);
            entity.setId(4L);
            return entity;
        });

        var resultado = service.criar(request());

        assertEquals(4L, resultado.id());
        assertEquals(3L, resultado.usuarioId());
        assertEquals("Autor de teste", resultado.usuarioNome());
        assertEquals(8L, resultado.oportunidadeId());
        assertEquals("Ligação com cliente", resultado.descricao());
        verify(vinculos).resolver(null, null, 8L, true);
        verify(vinculos).usuarioAtivo(3L);
    }

    @Test
    void atualizarPreservaIdEDataCriacao() {
        prepararVinculos();
        Interacao entity = new Interacao();
        entity.setId(4L);
        LocalDateTime data = LocalDateTime.of(2026, 10, 1, 8, 0);
        Usuario autor = new Usuario();
        autor.setId(3L);
        entity.setUsuario(autor);
        entity.setDataCriacao(data);
        when(repository.findById(4L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(entity)).thenReturn(entity);

        var resultado = service.atualizar(4L, request());

        assertEquals(4L, resultado.id());
        assertEquals(data, resultado.dataCriacao());
        assertEquals(8L, resultado.oportunidadeId());
    }

    @Test
    void vinculoInvalidoImpedePersistencia() {
        when(vinculos.resolver(null, null, 8L, true)).thenThrow(
                new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Vínculo incompatível"));
        assertEquals(422, assertThrows(ResponseStatusException.class,
                () -> service.criar(request())).getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void buscarAusenteRetorna404() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.buscarPorId(99L)).getStatusCode().value());
    }

    @Test
    void excluirRemoveEConfirmaNoBanco() {
        Interacao entity = new Interacao();
        Usuario autor = new Usuario();
        autor.setId(3L);
        entity.setUsuario(autor);
        when(repository.findById(4L)).thenReturn(Optional.of(entity));
        service.excluir(4L);
        verify(repository).delete(entity);
        verify(repository).flush();
    }

    @Test
    void editarNaoPermiteTransferirAutoriaDeOutroUsuario() {
        Interacao entity = new Interacao();
        Usuario autor = new Usuario();
        autor.setId(4L);
        entity.setUsuario(autor);
        entity.setDescricao("Texto original");
        when(repository.findById(8L)).thenReturn(Optional.of(entity));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Autoria diferente"))
                .when(vinculos).validarAutoria(4L);

        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.atualizar(8L, request())).getStatusCode().value());

        assertEquals("Texto original", entity.getDescricao());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void excluirRegistroDeOutroAutorRetorna403() {
        Interacao entity = new Interacao();
        Usuario autor = new Usuario();
        autor.setId(4L);
        entity.setUsuario(autor);
        when(repository.findById(8L)).thenReturn(Optional.of(entity));
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Autoria diferente"))
                .when(vinculos).validarAutoria(4L);

        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> service.excluir(8L)).getStatusCode().value());

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }

    private InteracaoRequest request() {
        return new InteracaoRequest(TipoInteracao.CHAMADA, "Ligação com cliente", LocalDateTime.of(2026, 10, 16, 10, 0), 3L, null, null, 8L);
    }

    private void prepararVinculos() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNome("Autor de teste");
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.setId(8L);
        when(vinculos.resolver(null, null, 8L, true))
                .thenReturn(new VinculosAcompanhamentoService.Vinculos(null, null, oportunidade));
        lenient().when(vinculos.autor(3L)).thenReturn(3L);
        when(vinculos.usuarioAtivo(3L)).thenReturn(usuario);
    }
}
