package com.ibrowse.crm.discador;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.ibrowse.crm.discador.VonixClient.ResultadoDiscagem;

class VonixClientTest {

    private static final String URL = "https://credvalor.api.vonixcc.com.br";
    private MockRestServiceServer servidor;
    private VonixClient client;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder();
        servidor = MockRestServiceServer.bindTo(builder).build();
        client = new VonixClient(new VonixProperties(true, URL, "tok123", "77", "3,1800", 300000, 5), builder);
    }

    @Test
    void adicionarContatoMontaQueryDaVonixComTokenBearerEDadosCodificados() {
        servidor.expect(request -> {
                    URI uri = request.getURI();
                    assertEquals("/v1/contact/INT-10", uri.getPath());
                    var params = UriComponentsBuilder.fromUri(uri).build(true).getQueryParams();
                    assertEquals("Jos%C3%A9%20%26%20Filhos", params.getFirst("contact_name"));
                    assertEquals("77", params.getFirst("queue"));
                    assertEquals("11987654321", params.getFirst("to%5B1%5D"));
                    assertEquals("3%2C1800", params.getFirst("retry"));
                    assertEquals("Loja%20%2B%20Cia", params.getFirst("contact_data%5Bempresa%5D"));
                    assertFalse(params.containsKey("contact_data%5Bvazio%5D"), "dado vazio não deve ser enviado");
                })
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer tok123"))
                .andRespond(withSuccess("\"sucess\"", MediaType.APPLICATION_JSON));

        Map<String, String> dados = new LinkedHashMap<>();
        dados.put("empresa", "Loja + Cia");
        dados.put("vazio", " ");
        client.adicionarContato("INT-10", "José & Filhos", "11987654321", dados);
        servidor.verify();
    }

    @Test
    void erroDaVonixViraExcecaoParaOServicoRegistrar() {
        servidor.expect(requestTo(org.hamcrest.Matchers.startsWith(URL + "/v1/contact/INT-1")))
                .andRespond(withBadRequest().body("queue not found"));
        assertThrows(HttpClientErrorException.class,
                () -> client.adicionarContato("INT-1", "Ana", "11987654321", Map.of()));
    }

    @Test
    void removerContatoUsaDelete() {
        servidor.expect(requestTo(URL + "/v1/contact/INT-5"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());
        client.removerContato("INT-5");
        servidor.verify();
    }

    @Test
    void listarFilasLeAcentosEmUtf8MesmoSemCharsetNaResposta() {
        servidor.expect(requestTo(URL + "/v1/queues")).andRespond(withSuccess(
                "<queues><queue id=\"h\"><name>Homologação</name></queue></queues>".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                MediaType.APPLICATION_XML));
        assertTrue(client.listarFilas().contains("Homologação"));
    }

    @Test
    void resultadosLeOFormatoRealDaVonix() {
        // Resposta real capturada no teste com a fila "teste" (números e ids de chamada trocados).
        servidor.expect(requestTo(URL + "/v1/results")).andRespond(withSuccess("""
                <?xml version="1.0" encoding="UTF-8"?>
                <results feeder_id="luiz"><contact status="success" will_retry="no" queue="teste" id="INT-8"><number duration="6" \
                destination="11999990000" dialed_at="29/10/26 01:20:27" hangup_at="29/10/26 01:20:50" id="1" \
                answered_at="29/10/26 01:20:44" callfilename="1.MP3" callid="1.1" ivr_digits="null">16</number></contact>\
                <contact status="failure" will_retry="yes" queue="teste" id="INT-9"><number destination="11999990000" \
                dialed_at="29/10/26 01:20:50" hangup_at="29/10/26 01:20:53" id="1" callid="1.2" ivr_digits="null">41</number></contact>\
                </results>""".getBytes(java.nio.charset.StandardCharsets.UTF_8), MediaType.APPLICATION_XML));

        var resultados = client.resultados();
        assertEquals(2, resultados.size());
        assertEquals(new ResultadoDiscagem("INT-8", "teste", "success", false), resultados.get(0));
        assertEquals(new ResultadoDiscagem("INT-9", "teste", "failure", true), resultados.get(1));
    }

    @Test
    void resultadosAceitaTagsFilhasEIgnoraRespostaInvalida() {
        var lidos = VonixClient.lerResultados(
                "<results><contact><id>INT-9</id><status>failure</status></contact></results>");
        assertEquals(1, lidos.size());
        assertEquals("INT-9", lidos.get(0).contactId());
        assertTrue(VonixClient.lerResultados("{\"erro\":true}").isEmpty());
        assertTrue(VonixClient.lerResultados("<results><contact").isEmpty());
    }

    @Test
    void xmlComDoctypeEhRecusado() {
        String xxe = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"file:///etc/passwd\">]>"
                + "<results><contact id=\"&x;\" status=\"success\"/></results>";
        assertTrue(VonixClient.lerResultados(xxe).isEmpty());
    }
}
