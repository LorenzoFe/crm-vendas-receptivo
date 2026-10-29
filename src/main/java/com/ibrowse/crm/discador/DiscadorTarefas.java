package com.ibrowse.crm.discador;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscadorTarefas {

    private final DiscadorService discador;
    private final VonixProperties properties;

    @Async
    @TransactionalEventListener
    public void aoRegistrarInteresse(InteresseRegistradoEvent evento) {
        try {
            discador.enviar(evento.interesseId());
        } catch (RuntimeException erro) {
            log.error("Erro inesperado ao enviar interesse {} ao discador", evento.interesseId(), erro);
        }
    }

    @Scheduled(initialDelay = 60_000, fixedDelayString = "${vonix.intervalo-sincronizacao-ms:300000}")
    public void sincronizar() {
        if (!properties.pronto()) {
            return;
        }
        for (Long id : discador.idsParaReenvio()) {
            try {
                discador.enviar(id);
            } catch (RuntimeException erro) {
                log.error("Erro inesperado ao reenviar interesse {} ao discador", id, erro);
            }
        }
        try {
            int atualizados = discador.sincronizarResultados();
            if (atualizados > 0) {
                log.info("Discador: {} resultado(s) de discagem atualizado(s)", atualizados);
            }
        } catch (RestClientException erro) {
            log.warn("Falha ao buscar resultados na Vonix: {}", erro.getMessage());
        }
    }
}
