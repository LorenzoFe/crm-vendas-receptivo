package com.ibrowse.crm.service;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.repository.PropostaRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NumeroPropostaService {

    private static final int CHAVE_CONTADOR = 1347571536;
    private final EntityManager entityManager;
    private final PropostaRepository propostaRepository;

    /** O lock permanece até o commit da mesma transação que persiste a proposta. */
    @Transactional(propagation = Propagation.MANDATORY)
    public String proximo(LocalDate dataEmissao) {
        entityManager.createNativeQuery("select 1 from pg_advisory_xact_lock(cast(:chave as integer))")
                .setParameter("chave", CHAVE_CONTADOR)
                .getSingleResult();
        String prefixo = "PROP-" + dataEmissao.getYear() + "-";
        int ultimo = propostaRepository.buscarMaiorNumeroValido("^" + prefixo + "[0-9]{6}$")
                .map(numero -> Integer.parseInt(numero.substring(prefixo.length())))
                .orElse(0);
        if (ultimo >= 999999) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O limite anual de números de proposta foi atingido");
        }
        return prefixo + String.format(Locale.ROOT, "%06d", ultimo + 1);
    }
}
