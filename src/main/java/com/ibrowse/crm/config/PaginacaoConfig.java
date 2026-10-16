package com.ibrowse.crm.config;

import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class PaginacaoConfig {

    private PaginacaoConfig() {
    }

    public static void validar(Pageable pageable, Set<String> camposOrdenacao) {
        if (pageable == null || pageable.isUnpaged() || pageable.getPageSize() > 100
                || pageable.getOffset() > Integer.MAX_VALUE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe página com tamanho entre 1 e 100");
        }
        if (pageable.getSort().stream().anyMatch(ordem -> !camposOrdenacao.contains(ordem.getProperty()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Campo de ordenação inválido");
        }
    }
}
