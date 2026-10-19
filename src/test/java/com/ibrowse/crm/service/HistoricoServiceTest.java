package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import com.ibrowse.crm.repository.OportunidadeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@ExtendWith(MockitoExtension.class)
class HistoricoServiceTest {

    @Mock OportunidadeRepository oportunidades;
    @Mock EntityManager entityManager;
    @Mock Query contagem;
    @Mock Query consulta;
    HistoricoService service;

    @BeforeEach
    void preparar() {
        service = new HistoricoService(oportunidades, entityManager);
    }

    @Test
    void paginaUniaoNoBancoEMantemOrdemRecebidaDasDuasOrigens() {
        prepararConsultas(5L);
        LocalDateTime data = LocalDateTime.of(2026, 10, 16, 10, 0);
        when(consulta.getResultList()).thenReturn(Arrays.asList(
                new Object[] { 9L, "INTERACAO", "EMAIL", "Enviou orçamento", Timestamp.valueOf(data), 3L },
                new Object[] { 12L, "NOTA", null, "Cliente avaliando", data, 3L }));

        var resultado = service.listar(7L, PageRequest.of(1, 2));

        assertEquals(5, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        assertEquals(List.of("INTERACAO", "NOTA"), resultado.getContent().stream().map(item -> item.origem()).toList());
        assertEquals(data, resultado.getContent().getFirst().dataEvento());
        assertNull(resultado.getContent().get(1).tipo());
        verify(consulta).setParameter("oportunidadeId", 7L);
        verify(contagem).setParameter("oportunidadeId", 7L);
        verify(consulta).setParameter("limite", 2);
        verify(consulta).setParameter("deslocamento", 2L);
        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(entityManager).createNativeQuery(argThat(sql ->
                sql.contains("UNION ALL") && sql.contains("ORDER BY data_evento DESC, origem ASC, id DESC")
                && sql.contains("LIMIT :limite OFFSET :deslocamento")));
    }

    @Test
    void paginaForaDoTotalMantemContagemReal() {
        prepararConsultas(3L);
        when(consulta.getResultList()).thenReturn(List.of());

        var resultado = service.listar(7L, PageRequest.of(4, 2));

        assertTrue(resultado.isEmpty());
        assertEquals(3, resultado.getTotalElements());
        verify(consulta).setParameter("deslocamento", 8L);
    }

    @Test
    void oportunidadeSemHistoricoRetornaPaginaVazia() {
        prepararConsultas(0L);
        when(consulta.getResultList()).thenReturn(List.of());
        assertEquals(0, service.listar(7L, PageRequest.of(0, 20)).getTotalElements());
    }

    @Test
    void oportunidadeInexistenteRetorna404AntesDoSql() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.listar(99L, PageRequest.of(0, 20))).getStatusCode().value());
        verifyNoInteractions(entityManager);
    }

    private void prepararConsultas(long total) {
        when(oportunidades.existsById(7L)).thenReturn(true);
        when(entityManager.createNativeQuery(startsWith("SELECT count(*)"))).thenReturn(contagem);
        when(entityManager.createNativeQuery(startsWith("SELECT *"))).thenReturn(consulta);
        when(contagem.setParameter(anyString(), any())).thenReturn(contagem);
        when(consulta.setParameter(anyString(), any())).thenReturn(consulta);
        when(contagem.getSingleResult()).thenReturn(total);
    }
}
