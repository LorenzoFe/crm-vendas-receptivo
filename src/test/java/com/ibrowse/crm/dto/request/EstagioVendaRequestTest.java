package com.ibrowse.crm.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class EstagioVendaRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void fechar() {
        FACTORY.close();
    }

    @Test
    void aceitaLimitesZeroECem() {
        assertTrue(VALIDATOR.validate(new EstagioVendaRequest("Inicial", 1, 0)).isEmpty());
        assertTrue(VALIDATOR.validate(new EstagioVendaRequest("Final", 2, 100)).isEmpty());
    }

    @Test
    void rejeitaProbabilidadeForaDosLimites() {
        assertFalse(VALIDATOR.validate(new EstagioVendaRequest("Inicial", 1, -1)).isEmpty());
        assertFalse(VALIDATOR.validate(new EstagioVendaRequest("Final", 2, 101)).isEmpty());
    }

    @Test
    void rejeitaCamposObrigatoriosAusentes() {
        assertEquals(3, VALIDATOR.validate(new EstagioVendaRequest(null, null, null)).size());
    }

    @Test
    void rejeitaNomeVazioOuMaiorQueCemCaracteres() {
        assertFalse(VALIDATOR.validate(new EstagioVendaRequest("   ", 1, 50)).isEmpty());
        assertFalse(VALIDATOR.validate(new EstagioVendaRequest("a".repeat(101), 1, 50)).isEmpty());
    }

    @Test
    void removeEspacosDasPontasSemAlterarTextoInterno() {
        var request = new EstagioVendaRequest("  Em negociação  ", 3, 70);

        assertEquals("Em negociação", request.nome());
        assertTrue(VALIDATOR.validate(request).isEmpty());
    }
}
