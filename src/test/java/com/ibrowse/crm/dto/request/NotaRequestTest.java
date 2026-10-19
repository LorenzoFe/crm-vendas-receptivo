package com.ibrowse.crm.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class NotaRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fechar() {
        FACTORY.close();
    }

    @Test
    void aceitaDadosValidos() {
        assertTrue(VALIDATOR.validate(new NotaRequest("Anotação", 1L, 2L, null, null)).isEmpty());
    }

    @Test
    void camposObrigatoriosAusentesSaoRecusados() {
        assertEquals(2, VALIDATOR.validate(new NotaRequest(" ", null, null, null, null)).size());
    }

    @Test
    void identificadoresNegativosSaoRecusados() {
        assertEquals(2, VALIDATOR.validate(new NotaRequest("Anotação", -1L, -2L, null, null)).size());
    }
}
