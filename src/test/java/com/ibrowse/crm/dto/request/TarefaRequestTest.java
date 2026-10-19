package com.ibrowse.crm.dto.request;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import com.ibrowse.crm.enums.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class TarefaRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fechar() {
        FACTORY.close();
    }

    @Test
    void aceitaDadosValidos() {
        assertTrue(VALIDATOR.validate(new TarefaRequest("Retornar", null, 1L, StatusTarefa.PENDENTE, PrioridadeTarefa.MEDIA, null, null, null, null)).isEmpty());
    }

    @Test
    void camposObrigatoriosAusentesSaoRecusados() {
        assertEquals(3, VALIDATOR.validate(new TarefaRequest(" ", null, null, null, null, null, null, null, null)).size());
    }

    @Test
    void identificadoresNegativosSaoRecusados() {
        assertEquals(2, VALIDATOR.validate(new TarefaRequest("Retornar", null, -1L, StatusTarefa.PENDENTE, PrioridadeTarefa.MEDIA, null, -2L, null, null)).size());
    }
}
