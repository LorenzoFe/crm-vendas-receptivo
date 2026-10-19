package com.ibrowse.crm.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.entity.Oportunidade;
import com.ibrowse.crm.entity.Proposta;
import com.ibrowse.crm.enums.StatusOportunidade;
import com.ibrowse.crm.enums.StatusProposta;
import com.ibrowse.crm.mapper.PropostaMapper;
import com.ibrowse.crm.repository.PropostaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

@ExtendWith(MockitoExtension.class)
class PropostaServiceTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 16);
    @Mock private PropostaRepository repository;
    @Mock private NumeroPropostaService numeros;
    @Mock private EntityManager entityManager;
    @Mock private TypedQuery<BigDecimal> subtotais;
    private PropostaService service;
    private Oportunidade oportunidade;
    private Proposta proposta;

    @BeforeEach
    void preparar() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-16T12:00:00Z"), ZoneOffset.UTC);
        service = new PropostaService(repository, Mappers.getMapper(PropostaMapper.class), numeros,
                entityManager, clock);
        oportunidade = new Oportunidade();
        oportunidade.setId(3L);
        oportunidade.setStatus(StatusOportunidade.ATIVA);
        proposta = new Proposta();
        proposta.setId(8L);
        proposta.setOportunidade(oportunidade);
        proposta.setValorTotal(new BigDecimal("120.40"));
        proposta.setStatus(StatusProposta.ENVIADA);
    }

    @Test
    void geraRascunhoSomandoSubtotaisPersistidosComDatasDeterministicas() {
        when(entityManager.find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);
        prepararSubtotais(List.of(new BigDecimal("100.10"), new BigDecimal("20.30")));
        when(numeros.proximo(HOJE)).thenReturn("PROP-2026-000001");
        when(repository.saveAndFlush(any())).thenAnswer(chamada -> {
            Proposta nova = chamada.getArgument(0);
            nova.setId(9L);
            return nova;
        });

        var response = service.gerarDeOportunidade(3L);

        assertEquals(9L, response.id());
        assertEquals(3L, response.oportunidadeId());
        assertEquals(new BigDecimal("120.40"), response.valorTotal());
        assertEquals("PROP-2026-000001", response.numero());
        assertEquals(StatusProposta.RASCUNHO, response.status());
        assertEquals(HOJE, response.dataEmissao());
        assertEquals(HOJE.plusDays(15), response.dataVencimento());
        assertNull(response.dataAceitacao());
        var ordem = inOrder(entityManager, subtotais, numeros, repository);
        ordem.verify(entityManager).find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE);
        ordem.verify(entityManager).createQuery(anyString(), eq(BigDecimal.class));
        ordem.verify(subtotais).getResultList();
        ordem.verify(numeros).proximo(HOJE);
        ordem.verify(repository).saveAndFlush(any());
    }

    @Test
    void semItensRetorna422SemConsumirNumero() {
        when(entityManager.find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);
        prepararSubtotais(List.of());

        assertStatus(422, () -> service.gerarDeOportunidade(3L));

        verifyNoInteractions(numeros);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void subtotalNuloOuNegativoETotalAcimaDoLimiteSaoRecusados() {
        when(entityManager.find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);
        prepararSubtotais(Arrays.asList((BigDecimal) null));
        assertStatus(422, () -> service.gerarDeOportunidade(3L));
        when(subtotais.getResultList()).thenReturn(List.of(new BigDecimal("-0.01")));
        assertStatus(422, () -> service.gerarDeOportunidade(3L));
        when(subtotais.getResultList()).thenReturn(List.of(new BigDecimal("9999999999999.99"), BigDecimal.ONE));
        assertStatus(422, () -> service.gerarDeOportunidade(3L));
        verifyNoInteractions(numeros);
    }

    @ParameterizedTest
    @MethodSource("oportunidadesInativas")
    void geracaoRecusaOportunidadeInativa(StatusOportunidade status) {
        oportunidade.setStatus(status);
        when(entityManager.find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);
        assertStatus(409, () -> service.gerarDeOportunidade(3L));
        verifyNoInteractions(numeros);
    }

    static Stream<StatusOportunidade> oportunidadesInativas() {
        return Arrays.stream(StatusOportunidade.values()).filter(status -> status != StatusOportunidade.ATIVA);
    }

    @Test
    void aceitarAtualizaPropostaEOportunidadeSobLocksNaOrdemGlobal() {
        prepararBloqueio();
        when(repository.saveAndFlush(proposta)).thenReturn(proposta);

        var response = service.aceitar(8L);

        assertEquals(StatusProposta.ACEITA, response.status());
        assertEquals(HOJE, response.dataAceitacao());
        assertEquals(StatusOportunidade.GANHA, oportunidade.getStatus());
        assertEquals(proposta.getValorTotal(), oportunidade.getValorReal());
        assertEquals(HOJE, oportunidade.getDataFechamentoReal());
        var ordem = inOrder(entityManager, repository);
        ordem.verify(repository).buscarOportunidadeId(8L);
        ordem.verify(entityManager).find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE);
        ordem.verify(entityManager).find(Proposta.class, 8L, LockModeType.PESSIMISTIC_WRITE);
        ordem.verify(repository).saveAndFlush(proposta);
    }

    @Test
    void segundaAceitacaoSimuladaEncontraEstadoConfirmadoERecusaNovaEscrita() {
        prepararBloqueio();
        when(repository.saveAndFlush(proposta)).thenReturn(proposta);
        service.aceitar(8L);

        assertStatus(409, () -> service.aceitar(8L));

        verify(repository, times(1)).saveAndFlush(proposta);
    }

    @Test
    void outraPropostaNaoPodeSerAceitaAposOportunidadeGanha() {
        prepararBloqueio();
        oportunidade.setStatus(StatusOportunidade.GANHA);

        assertStatus(409, () -> service.aceitar(8L));

        assertEquals(StatusProposta.ENVIADA, proposta.getStatus());
        assertNull(proposta.getDataAceitacao());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void rejeicaoPreservaOportunidadeAtivaSemValorOuDataDeFechamento() {
        prepararBloqueio();
        when(repository.saveAndFlush(proposta)).thenReturn(proposta);

        assertEquals(StatusProposta.REJEITADA, service.rejeitar(8L).status());

        assertEquals(StatusOportunidade.ATIVA, oportunidade.getStatus());
        assertNull(oportunidade.getValorReal());
        assertNull(oportunidade.getDataFechamentoReal());
        assertNull(proposta.getDataAceitacao());
    }

    @Test
    void envioPermiteSomenteRascunhoERecusaRepeticao() {
        prepararBloqueio();
        proposta.setStatus(StatusProposta.RASCUNHO);
        when(repository.saveAndFlush(proposta)).thenReturn(proposta);

        assertEquals(StatusProposta.ENVIADA, service.enviar(8L).status());
        assertStatus(409, () -> service.enviar(8L));

        verify(repository, times(1)).saveAndFlush(proposta);
    }

    @ParameterizedTest
    @MethodSource("transicoesInvalidas")
    void transicaoInvalidaNaoPersiste(String acao, StatusProposta status) {
        prepararBloqueio();
        proposta.setStatus(status);
        assertStatus(409, () -> executar(acao));
        verify(repository, never()).saveAndFlush(any());
        assertEquals(StatusOportunidade.ATIVA, oportunidade.getStatus());
    }

    static Stream<Arguments> transicoesInvalidas() {
        return Stream.of("enviar", "aceitar", "rejeitar").flatMap(acao -> Arrays.stream(StatusProposta.values())
                .filter(status -> status != (acao.equals("enviar") ? StatusProposta.RASCUNHO : StatusProposta.ENVIADA))
                .map(status -> Arguments.of(acao, status)));
    }

    @Test
    void oportunidadeOuPropostaInexistenteRetorna404() {
        assertStatus(404, () -> service.gerarDeOportunidade(99L));
        assertStatus(404, () -> service.aceitar(99L));
        assertStatus(404, () -> service.buscarPorId(99L));
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void conflitoDeIntegridadeVira409() {
        prepararBloqueio();
        proposta.setStatus(StatusProposta.RASCUNHO);
        when(repository.saveAndFlush(proposta)).thenThrow(new DataIntegrityViolationException("simulado"));
        assertStatus(409, () -> service.enviar(8L));
    }

    @Test
    void listaFiltraPaginaEConverteSomenteIdsDasRelacoes() {
        var pagina = PageRequest.of(1, 10, Sort.by("numero"));
        when(repository.filtrar(StatusProposta.ENVIADA, 3L, pagina))
                .thenReturn(new PageImpl<>(List.of(proposta), pagina, 11));

        var resultado = service.listar(StatusProposta.ENVIADA, 3L, pagina);

        assertEquals(11, resultado.getTotalElements());
        assertEquals(1, resultado.getNumber());
        assertEquals(3L, resultado.getContent().getFirst().oportunidadeId());
    }

    @Test
    void idsOuPaginacaoInvalidosRetornam400AntesDeConsultar() {
        assertStatus(400, () -> service.enviar(0L));
        assertStatus(400, () -> service.gerarDeOportunidade(-1L));
        assertStatus(400, () -> service.listar(null, 0L, PageRequest.of(0, 20)));
        assertStatus(400, () -> service.listar(null, null, PageRequest.of(0, 101)));
        assertStatus(400, () -> service.listar(null, null, PageRequest.of(0, 20, Sort.by("oportunidade.empresa"))));
        verifyNoInteractions(repository, entityManager);
    }

    private void executar(String acao) {
        switch (acao) {
            case "enviar" -> service.enviar(8L);
            case "aceitar" -> service.aceitar(8L);
            case "rejeitar" -> service.rejeitar(8L);
            default -> throw new IllegalArgumentException(acao);
        }
    }

    private void prepararBloqueio() {
        when(repository.buscarOportunidadeId(8L)).thenReturn(Optional.of(3L));
        when(entityManager.find(Oportunidade.class, 3L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(oportunidade);
        when(entityManager.find(Proposta.class, 8L, LockModeType.PESSIMISTIC_WRITE)).thenReturn(proposta);
    }

    private void prepararSubtotais(List<BigDecimal> valores) {
        when(entityManager.createQuery(anyString(), eq(BigDecimal.class))).thenReturn(subtotais);
        when(subtotais.setParameter("oportunidadeId", 3L)).thenReturn(subtotais);
        when(subtotais.getResultList()).thenReturn(valores);
    }

    private void assertStatus(int esperado, Runnable acao) {
        assertEquals(esperado, assertThrows(ResponseStatusException.class, acao::run).getStatusCode().value());
    }
}
