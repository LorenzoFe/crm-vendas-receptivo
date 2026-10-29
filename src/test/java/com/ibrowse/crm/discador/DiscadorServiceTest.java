package com.ibrowse.crm.discador;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import com.ibrowse.crm.discador.VonixClient.ResultadoDiscagem;
import com.ibrowse.crm.entity.Interesse;
import com.ibrowse.crm.enums.StatusDiscador;
import com.ibrowse.crm.enums.StatusInteresse;
import com.ibrowse.crm.repository.InteresseRepository;

@ExtendWith(MockitoExtension.class)
class DiscadorServiceTest {

    private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-10-29T12:00:00Z"), ZoneId.of("UTC"));
    private static final VonixProperties LIGADO = new VonixProperties(true, "https://x", "tok", "77", "3,1800", 300000, 5);
    private static final VonixProperties DESLIGADO = new VonixProperties(false, "https://x", "tok", "77", "3,1800", 300000, 5);

    @Mock private VonixClient vonix;
    @Mock private InteresseRepository repository;

    private DiscadorService servico(VonixProperties properties) {
        return new DiscadorService(vonix, properties, repository, RELOGIO);
    }

    private Interesse interesse(StatusDiscador discador) {
        Interesse interesse = new Interesse();
        interesse.setId(10L);
        interesse.setNome("Ana");
        interesse.setTelefone("11987654321");
        interesse.setEmpresaNome("Loja");
        interesse.setStatus(StatusInteresse.NOVO);
        interesse.setDiscadorStatus(discador);
        return interesse;
    }

    @Test
    void statusInicialDependeDoTelefone() {
        Interesse semTelefone = interesse(null);
        semTelefone.setTelefone(null);
        assertEquals(StatusDiscador.SEM_TELEFONE, DiscadorService.statusInicial(semTelefone));
        assertEquals(StatusDiscador.PENDENTE, DiscadorService.statusInicial(interesse(null)));
    }

    @Test
    void enviaParaAFilaEMarcaNaFila() {
        Interesse interesse = interesse(StatusDiscador.PENDENTE);
        when(repository.findById(10L)).thenReturn(Optional.of(interesse));

        servico(LIGADO).enviar(10L);

        verify(vonix).adicionarContato(eq("INT-10"), eq("Ana"), eq("11987654321"), anyMap());
        assertEquals(StatusDiscador.NA_FILA, interesse.getDiscadorStatus());
        assertEquals(1, interesse.getDiscadorTentativas());
        assertNotNull(interesse.getDiscadorEnviadoEm());
    }

    @Test
    void desligadoNaoChamaAVonixEContinuaPendente() {
        Interesse interesse = interesse(StatusDiscador.PENDENTE);
        when(repository.findById(10L)).thenReturn(Optional.of(interesse));

        servico(DESLIGADO).enviar(10L);

        verifyNoInteractions(vonix);
        assertEquals(StatusDiscador.PENDENTE, interesse.getDiscadorStatus());
    }

    @Test
    void falhaDaVonixFicaRegistradaSemLancarExcecao() {
        Interesse interesse = interesse(StatusDiscador.PENDENTE);
        when(repository.findById(10L)).thenReturn(Optional.of(interesse));
        doThrow(new ResourceAccessException("timeout")).when(vonix)
                .adicionarContato(anyString(), anyString(), anyString(), anyMap());

        assertDoesNotThrow(() -> servico(LIGADO).enviar(10L));
        assertEquals(StatusDiscador.ERRO, interesse.getDiscadorStatus());
        assertEquals("timeout", interesse.getDiscadorErro());
    }

    @Test
    void naoReenviaQuemJaEstaNaFila() {
        when(repository.findById(10L)).thenReturn(Optional.of(interesse(StatusDiscador.NA_FILA)));
        servico(LIGADO).enviar(10L);
        verifyNoInteractions(vonix);
    }

    @Test
    void sincronizaResultadosSoDeQuemEstaNaFila() {
        Interesse naFila = interesse(StatusDiscador.NA_FILA);
        Interesse removido = interesse(StatusDiscador.REMOVIDO);
        removido.setId(11L);
        when(vonix.resultados()).thenReturn(List.of(
                new ResultadoDiscagem("INT-10", "77", "ivrsuccess", false),
                new ResultadoDiscagem("INT-11", "77", "success", false),
                new ResultadoDiscagem("OUTRO-1", "77", "success", false),
                new ResultadoDiscagem("INT-12", "77", "desconhecido", false),
                new ResultadoDiscagem("INT-13", "77", "failure", true)));
        when(repository.findById(10L)).thenReturn(Optional.of(naFila));
        when(repository.findById(11L)).thenReturn(Optional.of(removido));

        assertEquals(1, servico(LIGADO).sincronizarResultados());
        assertEquals(StatusDiscador.CAIXA_POSTAL, naFila.getDiscadorStatus());
        assertNotNull(naFila.getDiscadorResultadoEm());
        assertEquals(StatusDiscador.REMOVIDO, removido.getDiscadorStatus());
    }

    @Test
    void descartarRemoveDaFilaEFalhaNaoImpedeODescarte() {
        Interesse naFila = interesse(StatusDiscador.NA_FILA);
        servico(LIGADO).removerDaFila(naFila);
        verify(vonix).removerContato("INT-10");
        assertEquals(StatusDiscador.REMOVIDO, naFila.getDiscadorStatus());

        Interesse comFalha = interesse(StatusDiscador.NA_FILA);
        doThrow(new ResourceAccessException("fora do ar")).when(vonix).removerContato("INT-10");
        assertDoesNotThrow(() -> servico(LIGADO).removerDaFila(comFalha));
        assertEquals(StatusDiscador.NA_FILA, comFalha.getDiscadorStatus());
        assertTrue(comFalha.getDiscadorErro().contains("fora do ar"));

        Interesse pendente = interesse(StatusDiscador.PENDENTE);
        servico(LIGADO).removerDaFila(pendente);
        assertEquals(StatusDiscador.REMOVIDO, pendente.getDiscadorStatus());
    }

    @Test
    void traduzStatusDaVonix() {
        assertEquals(StatusDiscador.ATENDIDO, DiscadorService.traduzir("success"));
        assertEquals(StatusDiscador.CAIXA_POSTAL, DiscadorService.traduzir("IVRSUCCESS"));
        assertEquals(StatusDiscador.NAO_ATENDIDO, DiscadorService.traduzir("failure"));
        assertNull(DiscadorService.traduzir("busy"));
        assertNull(DiscadorService.interesseId("INT-abc"));
        assertEquals(42L, DiscadorService.interesseId("INT-42"));
    }
}
