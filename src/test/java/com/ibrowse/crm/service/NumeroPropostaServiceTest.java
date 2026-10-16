package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.repository.PropostaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class NumeroPropostaServiceTest {

    @Mock private EntityManager entityManager;
    @Mock private PropostaRepository repository;
    @Mock private Query lock;
    private NumeroPropostaService service;

    @BeforeEach
    void preparar() {
        service = new NumeroPropostaService(entityManager, repository);
    }

    private void prepararLock() {
        when(entityManager.createNativeQuery("select 1 from pg_advisory_xact_lock(cast(:chave as integer))"))
                .thenReturn(lock);
        when(lock.setParameter(eq("chave"), anyInt())).thenReturn(lock);
    }

    @Test
    void primeiraPropostaDoAnoUsaUmEAdquireLockAntesDaConsulta() {
        prepararLock();
        assertEquals("PROP-2026-000001", service.proximo(LocalDate.of(2026, 10, 16)));
        var ordem = inOrder(lock, repository);
        ordem.verify(lock).getSingleResult();
        ordem.verify(repository).buscarMaiorNumeroValido("^PROP-2026-[0-9]{6}$");
    }

    @Test
    void usaMaiorNumeroMesmoComLacunasSemContarRegistros() {
        prepararLock();
        when(repository.buscarMaiorNumeroValido("^PROP-2026-[0-9]{6}$"))
                .thenReturn(Optional.of("PROP-2026-000042"));
        assertEquals("PROP-2026-000043", service.proximo(LocalDate.of(2026, 1, 1)));
        verify(repository, never()).count();
    }

    @Test
    void novaTransacaoSimuladaLeMaiorNumeroConfirmadoSobMesmoLock() {
        prepararLock();
        when(repository.buscarMaiorNumeroValido("^PROP-2026-[0-9]{6}$"))
                .thenReturn(Optional.of("PROP-2026-000041"), Optional.of("PROP-2026-000042"));
        assertEquals("PROP-2026-000042", service.proximo(LocalDate.of(2026, 10, 16)));
        assertEquals("PROP-2026-000043", service.proximo(LocalDate.of(2026, 10, 16)));
        verify(lock, times(2)).setParameter("chave", 1347571536);
        verify(lock, times(2)).getSingleResult();
    }

    @Test
    void mudancaDeAnoReiniciaSequencia() {
        prepararLock();
        assertEquals("PROP-2027-000001", service.proximo(LocalDate.of(2027, 1, 1)));
        verify(repository).buscarMaiorNumeroValido("^PROP-2027-[0-9]{6}$");
    }

    @Test
    void limiteAnualRetorna409SemGerarSetimoDigito() {
        prepararLock();
        when(repository.buscarMaiorNumeroValido("^PROP-2026-[0-9]{6}$"))
                .thenReturn(Optional.of("PROP-2026-999999"));
        var erro = assertThrows(ResponseStatusException.class,
                () -> service.proximo(LocalDate.of(2026, 10, 16)));
        assertEquals(409, erro.getStatusCode().value());
    }

    @Test
    void geradorExigeTransacaoDoChamadorParaLockDurarAtePersistencia() throws Exception {
        var anotacao = NumeroPropostaService.class.getMethod("proximo", LocalDate.class)
                .getAnnotation(Transactional.class);
        assertEquals(Propagation.MANDATORY, anotacao.propagation());
    }
}
