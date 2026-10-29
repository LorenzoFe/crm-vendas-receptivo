package com.ibrowse.crm.discador;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * Chamadas à API de Alimentação de Contatos/Discador da Vonix.
 * Documentação: https://clientes.vonixcc.com.br/v1/api-docs/contatos-discador
 */
@Slf4j
public class VonixClient {

    private final VonixProperties properties;
    private final RestClient http;

    public VonixClient(VonixProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.http = builder
                .baseUrl(properties.url())
                .defaultHeader(HttpHeaders.AUTHORIZATION, autorizacao(properties.token()))
                .build();
    }

    /** GET /v1/queues: filas que este token pode alimentar (resposta bruta da Vonix). */
    public String listarFilas() {
        return texto(http.get().uri("/v1/queues").retrieve().body(byte[].class));
    }

    /** POST /v1/contact/{contactId}: coloca o contato na fila configurada. */
    public void adicionarContato(String contactId, String nome, String telefone, Map<String, String> dados) {
        // Valores vão como variáveis para serem codificados por completo (acentos, &, +...).
        List<String[]> parametros = new ArrayList<>();
        parametros.add(new String[] { "contact_name", nome });
        parametros.add(new String[] { "queue", properties.filaId() });
        parametros.add(new String[] { "to[1]", telefone });
        if (properties.retry() != null && !properties.retry().isBlank()) {
            parametros.add(new String[] { "retry", properties.retry() });
        }
        dados.forEach((chave, valor) -> {
            if (valor != null && !valor.isBlank()) {
                parametros.add(new String[] { "contact_data[" + chave + "]", valor });
            }
        });

        Map<String, Object> variaveis = new HashMap<>();
        variaveis.put("contactId", contactId);
        http.post()
                .uri(uri -> {
                    uri.path("/v1/contact/{contactId}");
                    for (int i = 0; i < parametros.size(); i++) {
                        uri.queryParam(parametros.get(i)[0], "{p" + i + "}");
                        variaveis.put("p" + i, parametros.get(i)[1]);
                    }
                    return uri.build(variaveis);
                })
                .retrieve()
                .toBodilessEntity();
    }

    /** DELETE /v1/contact/{contactId}: tira o contato da fila. */
    public void removerContato(String contactId) {
        http.delete().uri("/v1/contact/{contactId}", contactId).retrieve().toBodilessEntity();
    }

    /**
     * GET /v1/results: resultado das discagens (XML).
     * Atenção: a Vonix entrega cada resultado uma única vez por alimentador (token). Só o CRM deve ler este endpoint.
     */
    public List<ResultadoDiscagem> resultados() {
        return lerResultados(texto(http.get().uri("/v1/results").retrieve().body(byte[].class)));
    }

    // A Vonix responde XML em UTF-8 sem informar o charset; sem isso os acentos chegam trocados.
    private static String texto(byte[] corpo) {
        return corpo == null ? null : new String(corpo, StandardCharsets.UTF_8);
    }

    /**
     * Lê o XML de resultados. Formato real da Vonix:
     * {@code <results feeder_id="..."><contact status="success" will_retry="no" queue="teste" id="INT-8"><number .../></contact></results>}.
     * Também aceita os dados como tags filhas, por segurança.
     */
    static List<ResultadoDiscagem> lerResultados(String xml) {
        List<ResultadoDiscagem> resultados = new ArrayList<>();
        if (xml == null || xml.isBlank()) {
            return resultados;
        }
        if (!xml.stripLeading().startsWith("<")) {
            log.warn("Resposta de /v1/results não é XML; ignorada: {}", resumo(xml));
            return resultados;
        }
        try {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setExpandEntityReferences(false);
            var leitor = fabrica.newDocumentBuilder();
            leitor.setErrorHandler(new DefaultHandler()); // erro vira exceção, sem imprimir no console
            NodeList contatos = leitor
                    .parse(new InputSource(new StringReader(xml)))
                    .getElementsByTagName("contact");
            for (int i = 0; i < contatos.getLength(); i++) {
                Element contato = (Element) contatos.item(i);
                String id = valor(contato, "id");
                String status = valor(contato, "status");
                if (id != null && status != null) {
                    boolean vaiRetentar = "yes".equalsIgnoreCase(valor(contato, "will_retry"));
                    resultados.add(new ResultadoDiscagem(id, valor(contato, "queue"), status, vaiRetentar));
                }
            }
        } catch (Exception erro) {
            log.warn("Não foi possível ler o XML de /v1/results: {} | início da resposta: {}", erro.getMessage(), resumo(xml));
        }
        return resultados;
    }

    private static String valor(Element elemento, String nome) {
        if (elemento.hasAttribute(nome)) {
            return elemento.getAttribute(nome).trim();
        }
        NodeList filhos = elemento.getElementsByTagName(nome);
        return filhos.getLength() > 0 ? filhos.item(0).getTextContent().trim() : null;
    }

    private static String resumo(String texto) {
        return texto.length() <= 300 ? texto : texto.substring(0, 300) + "...";
    }

    private static String autorizacao(String token) {
        if (token == null || token.isBlank()) {
            return "";
        }
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    /** {@code vaiRetentar}: a Vonix ainda fará novas tentativas, então o resultado não é final. */
    public record ResultadoDiscagem(String contactId, String fila, String status, boolean vaiRetentar) {
    }
}
