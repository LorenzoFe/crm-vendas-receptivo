package com.ibrowse.crm.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import com.ibrowse.crm.enums.TipoInteracao;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class InteracaoRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fechar() {
        FACTORY.close();
    }

    @Test
    void aceitaDadosValidos() {
        assertTrue(VALIDATOR.validate(new InteracaoRequest(TipoInteracao.CHAMADA, "Conversa", LocalDateTime.now(), 1L, 2L, null, null)).isEmpty());
    }

    @Test
    void camposObrigatoriosAusentesSaoRecusados() {
        assertEquals(4, VALIDATOR.validate(new InteracaoRequest(null, " ", null, null, null, null, null)).size());
    }

    @Test
    void identificadoresNegativosSaoRecusados() {
        assertEquals(2, VALIDATOR.validate(new InteracaoRequest(TipoInteracao.CHAMADA, "Conversa", LocalDateTime.now(), -1L, -2L, null, null)).size());
    }
}
