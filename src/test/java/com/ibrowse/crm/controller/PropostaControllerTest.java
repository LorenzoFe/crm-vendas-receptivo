package com.ibrowse.crm.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.response.PropostaResponse;
import com.ibrowse.crm.enums.StatusProposta;
import com.ibrowse.crm.service.PropostaService;

class PropostaControllerTest {

    private PropostaService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(PropostaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new PropostaController(service))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void gerarRetorna201LocationConsultavelEDtoFlat() throws Exception {
        when(service.gerarDeOportunidade(3L)).thenReturn(response(StatusProposta.RASCUNHO));

        mvc.perform(post("/propostas/gerar-de-oportunidade/3"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/propostas/8"))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.oportunidadeId").value(3))
                .andExpect(jsonPath("$.oportunidade").doesNotExist())
                .andExpect(jsonPath("$.valorTotal").value(120.40));
    }

    @Test
    void listaEncaminhaFiltrosEPaginacao() throws Exception {
        when(service.listar(eq(StatusProposta.ENVIADA), eq(3L), any()))
                .thenReturn(new PageImpl<>(List.of(response(StatusProposta.ENVIADA)), PageRequest.of(1, 5), 6));

        mvc.perform(get("/propostas").param("status", "ENVIADA").param("oportunidadeId", "3")
                .param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(6))
                .andExpect(jsonPath("$.content[0].status").value("ENVIADA"));

        verify(service).listar(eq(StatusProposta.ENVIADA), eq(3L),
                argThat(pagina -> pagina.getPageNumber() == 1 && pagina.getPageSize() == 5));
    }

    @Test
    void statusInexistenteOuIdMalformadoRetorna400SemChamarServico() throws Exception {
        mvc.perform(get("/propostas").param("status", "INVALIDO"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/propostas/abc/aceitar")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void todasAsRotasDeTransicaoRetornamDtoAtualizado() throws Exception {
        when(service.enviar(8L)).thenReturn(response(StatusProposta.ENVIADA));
        when(service.aceitar(8L)).thenReturn(response(StatusProposta.ACEITA));
        when(service.rejeitar(8L)).thenReturn(response(StatusProposta.REJEITADA));

        mvc.perform(put("/propostas/8/enviar")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADA"));
        mvc.perform(put("/propostas/8/aceitar")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITA"));
        mvc.perform(put("/propostas/8/rejeitar")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));
    }

    @Test
    void erroDeRegraDeNegocioMantem409Ou422() throws Exception {
        when(service.aceitar(8L)).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Estado inválido"));
        when(service.gerarDeOportunidade(3L)).thenThrow(
                new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT, "Sem itens"));

        mvc.perform(put("/propostas/8/aceitar")).andExpect(status().isConflict());
        mvc.perform(post("/propostas/gerar-de-oportunidade/3")).andExpect(status().is(422));
    }

    @Test
    void consultarRetornaPropostaOu404() throws Exception {
        when(service.buscarPorId(8L)).thenReturn(response(StatusProposta.RASCUNHO));
        when(service.buscarPorId(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Inexistente"));

        mvc.perform(get("/propostas/8")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(8));
        mvc.perform(get("/propostas/99")).andExpect(status().isNotFound());
    }

    private PropostaResponse response(StatusProposta status) {
        return new PropostaResponse(8L, "PROP-2026-000001", 3L, new BigDecimal("120.40"), status,
                LocalDate.of(2026, 10, 16), LocalDate.of(2026, 10, 31), null, null, null);
    }
}
