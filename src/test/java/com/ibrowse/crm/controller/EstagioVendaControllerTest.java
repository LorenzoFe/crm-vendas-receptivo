package com.ibrowse.crm.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.ibrowse.crm.dto.response.EstagioVendaResponse;
import com.ibrowse.crm.service.EstagioVendaService;

class EstagioVendaControllerTest {

    private EstagioVendaService service;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        service = mock(EstagioVendaService.class);
        mvc = MockMvcBuilders.standaloneSetup(new EstagioVendaController(service))
                .setControllerAdvice(new EstagioVendaExceptionHandler())
                .build();
    }

    @Test
    void cadastroRetorna201LocationEDto() throws Exception {
        when(service.criar(any())).thenReturn(
                new EstagioVendaResponse(7L, "Qualificação", 2, 30, LocalDateTime.of(2026, 10, 16, 10, 0)));

        mvc.perform(post("/estagios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Qualificação\",\"ordem\":2,\"probabilidadeConversao\":30}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/estagios/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.nome").value("Qualificação"));
    }

    @Test
    void entradaInvalidaRetorna400ComMensagem() throws Exception {
        mvc.perform(post("/estagios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\" \",\"ordem\":2,\"probabilidadeConversao\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.probabilidadeConversao").exists());

        verifyNoInteractions(service);
    }

    @Test
    void jsonMalformadoRetorna400SemDetalhesInternos() throws Exception {
        mvc.perform(post("/estagios").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("JSON ou parâmetro com formato inválido"));
    }

    @Test
    void idComFormatoInvalidoRetorna400() throws Exception {
        mvc.perform(get("/estagios/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void inexistenteRetorna404ComMotivo() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Estágio 99 não encontrado"));

        mvc.perform(get("/estagios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Estágio 99 não encontrado"));
    }

    @Test
    void nomeDuplicadoRetorna409() throws Exception {
        when(service.criar(any())).thenThrow(
                new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um estágio com esse nome"));

        mvc.perform(post("/estagios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Proposta\",\"ordem\":3,\"probabilidadeConversao\":50}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Já existe um estágio com esse nome"));
    }

    @Test
    void listarRetornaArrayMesmoQuandoVazio() throws Exception {
        when(service.listar()).thenReturn(List.of());

        mvc.perform(get("/estagios"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void editarRetorna200() throws Exception {
        when(service.atualizar(eq(7L), any())).thenReturn(
                new EstagioVendaResponse(7L, "Qualificação", 3, 40, null));

        mvc.perform(put("/estagios/7").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Qualificação\",\"ordem\":3,\"probabilidadeConversao\":40}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordem").value(3));
    }

    @Test
    void exclusaoLivreRetorna204() throws Exception {
        mvc.perform(delete("/estagios/7"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(service).excluir(7L);
    }

    @Test
    void exclusaoComVinculoRetorna409() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Estágio em uso"))
                .when(service).excluir(7L);

        mvc.perform(delete("/estagios/7"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("Estágio em uso")));
    }
}
