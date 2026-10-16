package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.dto.request.TarefaRequest;
import com.ibrowse.crm.entity.*;
import com.ibrowse.crm.enums.*;
import com.ibrowse.crm.mapper.TarefaMapper;
import com.ibrowse.crm.repository.TarefaRepository;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock TarefaRepository repository;
    @Mock VinculosAcompanhamentoService vinculos;
    TarefaService service;
    Clock clock = Clock.fixed(Instant.parse("2026-10-16T13:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void preparar() {
        service = new TarefaService(repository, Mappers.getMapper(TarefaMapper.class), vinculos, clock);
    }

    @Test
    void criarConcluidaDefineDataNoServidor() {
        prepararVinculos();
        when(repository.saveAndFlush(any())).thenAnswer(invocacao -> {
            Tarefa tarefa = invocacao.getArgument(0);
            tarefa.setId(1L);
            return tarefa;
        });

        var resposta = service.criar(request(StatusTarefa.CONCLUIDA));

        assertEquals(StatusTarefa.CONCLUIDA, resposta.status());
        assertEquals(LocalDateTime.now(clock), resposta.dataConclusao());
        assertEquals(3L, resposta.responsavelId());
    }

    @Test
    void concluirDefineStatusEDataAtualDoServidor() {
        Tarefa tarefa = tarefa(StatusTarefa.PENDENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.saveAndFlush(tarefa)).thenReturn(tarefa);

        var resposta = service.concluir(1L);
        assertEquals(StatusTarefa.CONCLUIDA, resposta.status());
        assertEquals(LocalDateTime.now(clock), resposta.dataConclusao());

        tarefa.setDataConclusao(LocalDateTime.of(2026, 10, 10, 9, 0));
        assertEquals(LocalDateTime.now(clock), service.concluir(1L).dataConclusao());
    }

    @Test
    void putReabrindoTarefaLimpaDataConclusao() {
        prepararVinculos();
        Tarefa tarefa = tarefa(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(LocalDateTime.now(clock).minusDays(1));
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.saveAndFlush(tarefa)).thenReturn(tarefa);

        var resposta = service.atualizar(1L, request(StatusTarefa.EM_ANDAMENTO));

        assertEquals(StatusTarefa.EM_ANDAMENTO, resposta.status());
        assertNull(resposta.dataConclusao());
        assertEquals(1L, resposta.id());
    }

    @Test
    void putConcluindoTarefaDefineData() {
        prepararVinculos();
        Tarefa tarefa = tarefa(StatusTarefa.PENDENTE);
        when(repository.findById(1L)).thenReturn(Optional.of(tarefa));
        when(repository.saveAndFlush(tarefa)).thenReturn(tarefa);

        assertEquals(LocalDateTime.now(clock),
                service.atualizar(1L, request(StatusTarefa.CONCLUIDA)).dataConclusao());
    }

    @Test
    void urgentesEnviaLimiteDoRelogioSemOrdenacaoQueSobreponhaPrioridadeSql() {
        Pageable pagina = PageRequest.of(1, 2);
        when(repository.buscarUrgentes(eq(LocalDate.of(2026, 10, 21)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tarefa(StatusTarefa.PENDENTE)), pagina, 5));

        var resultado = service.urgentes(5, pagina);

        assertEquals(5, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        verify(repository).buscarUrgentes(eq(LocalDate.of(2026, 10, 21)),
                argThat(valor -> valor.getPageNumber() == 1 && valor.getPageSize() == 2 && valor.getSort().isUnsorted()));
    }

    @Test
    void urgentesAceitaHojeESemAnoAcimaDoLimite() {
        when(repository.buscarUrgentes(any(), any())).thenReturn(new PageImpl<>(List.of()));
        service.urgentes(0, PageRequest.of(0, 20));
        service.urgentes(365, PageRequest.of(0, 20));
        verify(repository).buscarUrgentes(eq(LocalDate.of(2026, 10, 16)), any());
        verify(repository).buscarUrgentes(eq(LocalDate.of(2027, 10, 16)), any());
    }

    @Test
    void urgentesRejeitaDiasInvalidosSemConsultarBanco() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.urgentes(-1, PageRequest.of(0, 20))).getStatusCode().value());
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> service.urgentes(366, PageRequest.of(0, 20))).getStatusCode().value());
        verifyNoInteractions(repository);
    }

    @Test
    void tarefaInexistenteRetorna404() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.concluir(99L)).getStatusCode().value());
        verify(repository, never()).saveAndFlush(any());
    }

    private void prepararVinculos() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        when(vinculos.resolver(null, null, null, false))
                .thenReturn(new VinculosAcompanhamentoService.Vinculos(null, null, null));
        when(vinculos.usuarioAtivo(3L)).thenReturn(usuario);
    }

    private TarefaRequest request(StatusTarefa status) {
        return new TarefaRequest("Retornar ligação", "Conversar com cliente", 3L, status,
                PrioridadeTarefa.ALTA, LocalDate.now(clock), null, null, null);
    }

    private Tarefa tarefa(StatusTarefa status) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(1L);
        tarefa.setTitulo("Retornar ligação");
        tarefa.setStatus(status);
        tarefa.setPrioridade(PrioridadeTarefa.ALTA);
        return tarefa;
    }
}
