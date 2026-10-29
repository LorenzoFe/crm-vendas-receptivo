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

    public String listarFilas() {
        return texto(http.get().uri("/v1/queues").retrieve().body(byte[].class));
    }

    public void adicionarContato(String contactId, String nome, String telefone, Map<String, String> dados) {
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

    public void removerContato(String contactId) {
        http.delete().uri("/v1/contact/{contactId}", contactId).retrieve().toBodilessEntity();
    }

    public List<ResultadoDiscagem> resultados() {
        return lerResultados(texto(http.get().uri("/v1/results").retrieve().body(byte[].class)));
    }

    private static String texto(byte[] corpo) {
        return corpo == null ? null : new String(corpo, StandardCharsets.UTF_8);
    }

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
            leitor.setErrorHandler(new DefaultHandler());
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

    public record ResultadoDiscagem(String contactId, String fila, String status, boolean vaiRetentar) {
    }
}
