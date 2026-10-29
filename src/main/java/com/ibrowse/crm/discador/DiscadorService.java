package com.ibrowse.crm.discador;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.discador.VonixClient.ResultadoDiscagem;
import com.ibrowse.crm.entity.Interesse;
import com.ibrowse.crm.entity.Produto;
import com.ibrowse.crm.enums.StatusDiscador;
import com.ibrowse.crm.enums.StatusInteresse;
import com.ibrowse.crm.repository.InteresseRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Liga os interesses à fila do discador da Vonix: envia, acompanha o resultado e remove.
 * Falhas da Vonix nunca derrubam o fluxo do CRM: ficam registradas no interesse e são tentadas de novo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiscadorService {

    public static final String PREFIXO_CONTATO = "INT-";
    private static final Set<StatusDiscador> AGUARDANDO_ENVIO = EnumSet.of(StatusDiscador.PENDENTE, StatusDiscador.ERRO);
    private static final int TAMANHO_ERRO = 500;
    private static final int TAMANHO_DADO = 250;

    private final VonixClient vonix;
    private final VonixProperties properties;
    private final InteresseRepository repository;
    private final Clock clock;

    /** Situação inicial de um interesse recém-criado. */
    public static StatusDiscador statusInicial(Interesse interesse) {
        return interesse.getTelefone() == null ? StatusDiscador.SEM_TELEFONE : StatusDiscador.PENDENTE;
    }

    @Transactional
    public void enviar(Long interesseId) {
        Interesse interesse = repository.findById(interesseId).orElse(null);
        if (interesse == null || !AGUARDANDO_ENVIO.contains(interesse.getDiscadorStatus())
                || interesse.getStatus() == StatusInteresse.DESCARTADO) {
            return;
        }
        if (!properties.pronto()) {
            log.debug("Discador desligado ou sem token/fila; interesse {} continua PENDENTE", interesseId);
            return;
        }
        enviarParaFila(interesse);
    }

    /**
     * Envio manual (botão "Reenviar"). Se a Vonix recusar, devolve o interesse com status ERRO e a mensagem,
     * sem lançar exceção, para não desfazer o registro da tentativa.
     */
    @Transactional
    public Interesse reenviar(Long interesseId) {
        Interesse interesse = repository.findById(interesseId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Interesse " + interesseId + " não encontrado"));
        if (!properties.pronto()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Discador desligado ou sem token/fila configurados");
        }
        if (interesse.getStatus() == StatusInteresse.DESCARTADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Interesse descartado não volta para a fila");
        }
        if (interesse.getTelefone() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Interesse sem telefone");
        }
        if (interesse.getDiscadorStatus() == StatusDiscador.NA_FILA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Interesse já está na fila do discador");
        }
        enviarParaFila(interesse);
        return interesse;
    }

    /** Ao descartar: tira da fila se ainda estiver lá. Nunca impede o descarte. */
    public void removerDaFila(Interesse interesse) {
        if (interesse.getDiscadorStatus() == StatusDiscador.PENDENTE
                || interesse.getDiscadorStatus() == StatusDiscador.ERRO) {
            interesse.setDiscadorStatus(StatusDiscador.REMOVIDO);
            return;
        }
        if (interesse.getDiscadorStatus() != StatusDiscador.NA_FILA || !properties.pronto()) {
            return;
        }
        try {
            vonix.removerContato(contactId(interesse));
            interesse.setDiscadorStatus(StatusDiscador.REMOVIDO);
            interesse.setDiscadorErro(null);
        } catch (RestClientException erro) {
            log.warn("Falha ao remover interesse {} da fila da Vonix: {}", interesse.getId(), erro.getMessage());
            interesse.setDiscadorErro(limitar("Falha ao remover da fila: " + erro.getMessage(), TAMANHO_ERRO));
        }
    }

    /** Pendentes que ainda não chegaram à Vonix (ex.: ela estava fora do ar). */
    public List<Long> idsParaReenvio() {
        return repository.findTop50ByStatusNotAndDiscadorStatusInAndDiscadorTentativasLessThanOrderByIdAsc(
                StatusInteresse.DESCARTADO, AGUARDANDO_ENVIO, properties.maxTentativasEnvio())
                .stream().map(Interesse::getId).toList();
    }

    /** Lê GET /v1/results e atualiza os interesses que estão na fila. */
    @Transactional
    public int sincronizarResultados() {
        List<ResultadoDiscagem> resultados = vonix.resultados();
        int atualizados = 0;
        for (ResultadoDiscagem resultado : resultados) {
            Long id = interesseId(resultado.contactId());
            StatusDiscador novo = traduzir(resultado.status());
            if (id == null || novo == null || resultado.vaiRetentar()) {
                continue;
            }
            Interesse interesse = repository.findById(id).orElse(null);
            if (interesse != null && interesse.getDiscadorStatus() == StatusDiscador.NA_FILA) {
                interesse.setDiscadorStatus(novo);
                interesse.setDiscadorResultadoEm(LocalDateTime.now(clock));
                atualizados++;
            }
        }
        return atualizados;
    }

    public String listarFilas() {
        if (!properties.tokenConfigurado()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Token da Vonix não configurado");
        }
        try {
            return vonix.listarFilas();
        } catch (RestClientException erro) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao consultar a Vonix: " + erro.getMessage(), erro);
        }
    }

    public Map<String, Object> situacao() {
        Map<String, Object> situacao = new LinkedHashMap<>();
        situacao.put("habilitado", properties.habilitado());
        situacao.put("url", properties.url());
        situacao.put("tokenConfigurado", properties.tokenConfigurado());
        situacao.put("filaConfigurada", properties.filaConfigurada());
        situacao.put("filaId", properties.filaId());
        situacao.put("pronto", properties.pronto());
        return situacao;
    }

    static StatusDiscador traduzir(String status) {
        return switch (status == null ? "" : status.trim().toLowerCase()) {
            case "success" -> StatusDiscador.ATENDIDO;
            case "ivrsuccess" -> StatusDiscador.CAIXA_POSTAL;
            case "failure" -> StatusDiscador.NAO_ATENDIDO;
            default -> null;
        };
    }

    static Long interesseId(String contactId) {
        if (contactId == null || !contactId.startsWith(PREFIXO_CONTATO)) {
            return null;
        }
        try {
            return Long.valueOf(contactId.substring(PREFIXO_CONTATO.length()));
        } catch (NumberFormatException erro) {
            return null;
        }
    }

    private void enviarParaFila(Interesse interesse) {
        interesse.setDiscadorTentativas(interesse.getDiscadorTentativas() + 1);
        try {
            vonix.adicionarContato(contactId(interesse), interesse.getNome(), interesse.getTelefone(), dadosExtras(interesse));
            interesse.setDiscadorStatus(StatusDiscador.NA_FILA);
            interesse.setDiscadorEnviadoEm(LocalDateTime.now(clock));
            interesse.setDiscadorErro(null);
            log.info("Interesse {} enviado à fila {} da Vonix", interesse.getId(), properties.filaId());
        } catch (RestClientException erro) {
            log.warn("Falha ao enviar interesse {} à Vonix (tentativa {}): {}",
                    interesse.getId(), interesse.getDiscadorTentativas(), erro.getMessage());
            interesse.setDiscadorStatus(StatusDiscador.ERRO);
            interesse.setDiscadorErro(limitar(erro.getMessage(), TAMANHO_ERRO));
        }
    }

    private Map<String, String> dadosExtras(Interesse interesse) {
        Map<String, String> dados = new LinkedHashMap<>();
        dados.put("interesse_id", String.valueOf(interesse.getId()));
        dados.put("empresa", limitar(interesse.getEmpresaNome(), TAMANHO_DADO));
        dados.put("email", interesse.getEmail());
        dados.put("produtos", limitar(interesse.getProdutos().stream().map(Produto::getNome)
                .sorted().collect(Collectors.joining(", ")), TAMANHO_DADO));
        dados.put("preferencia_contato", interesse.getPreferenciaContato());
        dados.put("mensagem", limitar(interesse.getMensagem(), TAMANHO_DADO));
        return dados;
    }

    private static String contactId(Interesse interesse) {
        return PREFIXO_CONTATO + interesse.getId();
    }

    private static String limitar(String texto, int tamanho) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.strip();
        return limpo.length() <= tamanho ? limpo : limpo.substring(0, tamanho - 3) + "...";
    }
}
