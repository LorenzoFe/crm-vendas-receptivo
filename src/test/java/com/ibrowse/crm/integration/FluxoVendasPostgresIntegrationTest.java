package com.ibrowse.crm.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@EnabledIfEnvironmentVariable(named = "CRM_TEST_DB_URL",
        matches = "jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/crm_validacao")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=${CRM_TEST_DB_URL}",
        "spring.datasource.username=postgres",
        "spring.datasource.password=${CRM_TEST_DB_PASSWORD}",
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema-test.sql",
        "jwt.secret=segredo-apenas-de-teste-isolado-com-mais-de-32-caracteres"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FluxoVendasPostgresIntegrationTest {

    @LocalServerPort
    private int port;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private Clock clock;

    private final JsonMapper json = JsonMapper.builder().build();
    private final HttpClient client = HttpClient.newHttpClient();
    private Long usuarioId;
    private Long outroUsuarioId;
    private Long empresaId;
    private Long outraEmpresaId;
    private Long outroContatoId;
    private Long produtoId;
    private Long estagioInicialId;
    private String token;
    private String outroToken;

    @BeforeAll
    void prepararColunaCalculadaDoBancoIsolado() {
        String url = jdbc.execute((java.sql.Connection connection) -> connection.getMetaData().getURL());
        assertTrue(url.matches("jdbc:postgresql://127\\.0\\.0\\.1:[0-9]+/crm_validacao.*"));
        assertEquals("ALWAYS", jdbc.queryForObject("SELECT is_generated FROM information_schema.columns "
                + "WHERE table_name='itens_oportunidade' AND column_name='subtotal'", String.class));
    }

    @BeforeEach
    void prepararDadosFicticios() {
        jdbc.execute("TRUNCATE usuarios, roles, permissoes, usuario_roles, role_permissoes, empresas, contatos, produtos, "
                + "estagios_vendas, oportunidades, itens_oportunidade, propostas, tarefas, interacoes, notas "
                + "RESTART IDENTITY CASCADE");
        String senha = passwordEncoder.encode("senha-ficticia-integracao");
        usuarioId = jdbc.queryForObject("INSERT INTO usuarios(nome,email,senha,ativo) VALUES (?,?,?,true) RETURNING id",
                Long.class, "Usuário de teste", "vendedor@example.invalid", senha);
        outroUsuarioId = jdbc.queryForObject("INSERT INTO usuarios(nome,email,senha,ativo) VALUES (?,?,?,true) RETURNING id",
                Long.class, "Outro usuário de teste", "outro@example.invalid", senha);
        empresaId = jdbc.queryForObject("INSERT INTO empresas(nome_razao_social,ativo) VALUES ('Empresa de teste',true) RETURNING id",
                Long.class);
        outraEmpresaId = jdbc.queryForObject("INSERT INTO empresas(nome_razao_social,ativo) VALUES ('Outra empresa',true) RETURNING id",
                Long.class);
        outroContatoId = jdbc.queryForObject("INSERT INTO contatos(nome,empresa_id,ativo) VALUES ('Contato externo',?,true) RETURNING id",
                Long.class, outraEmpresaId);
        produtoId = jdbc.queryForObject("INSERT INTO produtos(nome,preco_unitario,ativo) VALUES ('Produto teste',12.50,true) RETURNING id",
                Long.class);
        jdbc.update("INSERT INTO estagios_vendas(nome,ordem,probabilidade_conversao) VALUES ('Etapa posterior',20,80)");
        estagioInicialId = jdbc.queryForObject("INSERT INTO estagios_vendas(nome,ordem,probabilidade_conversao) "
                + "VALUES ('Etapa inicial',10,25) RETURNING id", Long.class);
        jdbc.update("INSERT INTO roles(nome) VALUES ('ADMIN')");
        jdbc.update("INSERT INTO permissoes(nome,modulo) VALUES "
                + "('ver_oportunidade','oportunidades'),('criar_oportunidade','oportunidades'),"
                + "('editar_oportunidade','oportunidades'),('deletar_oportunidade','oportunidades'),"
                + "('criar_proposta','propostas'),('editar_proposta','propostas')");
        jdbc.update("INSERT INTO role_permissoes(role_id,permissao_id) SELECT r.id, p.id FROM roles r CROSS JOIN permissoes p");
        jdbc.update("INSERT INTO usuario_roles(usuario_id,role_id) SELECT u.id, r.id FROM usuarios u CROSS JOIN roles r");
        token = login("vendedor@example.invalid");
        outroToken = login("outro@example.invalid");
    }

    @Test
    void endpointsDoFluxoDeVendasExigemAutenticacao() {
        for (String caminho : List.of("/estagios", "/oportunidades", "/propostas",
                "/tarefas", "/interacoes", "/notas")) {
            requisitar("GET", caminho, null, 401, null);
        }
    }

    @Test
    void oportunidadeUsaEstagioInicialERecusaContatoDeOutraEmpresa() {
        long id = criarOportunidade();
        JsonNode oportunidade = requisitar("GET", "/oportunidades/" + id, null, 200, token);
        assertEquals("ATIVA", oportunidade.path("status").asText());
        assertEquals(estagioInicialId.longValue(), oportunidade.path("estagioId").asLong());
        requisitar("POST", "/oportunidades", Map.of("titulo", "Inválida", "empresaId", empresaId,
                "vendedorId", usuarioId, "contatoId", outroContatoId), 422, token);
        requisitar("POST", "/oportunidades", Map.of("titulo", " ", "empresaId", empresaId,
                "vendedorId", usuarioId), 400, token);
    }

    @Test
    void listagemFiltradaKanbanEPipelineUsamSomenteAtivasEIncluemEstagiosVazios() {
        long id = criarOportunidade();
        requisitar("POST", "/oportunidades", Map.of("titulo", "Perdida", "empresaId", empresaId,
                "vendedorId", usuarioId, "valorEstimado", 999), 201, token);
        requisitar("PUT", "/oportunidades/2/perder", Map.of("motivo", "Sem interesse"), 200, token);
        JsonNode pagina = requisitar("GET", "/oportunidades?status=ATIVA&empresaId=" + empresaId,
                null, 200, token);
        assertEquals(1, pagina.path("page").path("totalElements").asLong());
        JsonNode kanban = requisitar("GET", "/oportunidades/kanban", null, 200, token);
        assertEquals(2, kanban.size());
        assertEquals(1, kanban.get(0).path("oportunidades").size());
        assertEquals(0, kanban.get(1).path("oportunidades").size());
        assertEquals(id, kanban.get(0).path("oportunidades").get(0).path("id").asLong());
        JsonNode pipeline = requisitar("GET", "/oportunidades/pipeline", null, 200, token);
        assertEquals(1, pipeline.path("quantidadeTotal").asLong());
        assertEquals(100.00, pipeline.path("valorEstimadoTotal").asDouble(), 0.001);
        assertEquals(25.00, pipeline.path("valorPonderadoTotal").asDouble(), 0.001);
    }

    @Test
    void subtotalCalculadoNoPostgresEItemSemprePertenceAOportunidadeDaUrl() {
        long id = criarOportunidade();
        JsonNode item = requisitar("POST", "/oportunidades/" + id + "/itens",
                Map.of("produtoId", produtoId, "quantidade", 3), 201, token);
        assertEquals(37.50, item.path("subtotal").asDouble(), 0.001);
        long itemId = item.path("id").asLong();
        long outra = criarOportunidade();
        requisitar("PUT", "/oportunidades/" + outra + "/itens/" + itemId,
                Map.of("quantidade", 4), 404, token);
        JsonNode editado = requisitar("PUT", "/oportunidades/" + id + "/itens/" + itemId,
                Map.of("quantidade", 4), 200, token);
        assertEquals(50.00, editado.path("subtotal").asDouble(), 0.001);
        requisitar("DELETE", "/oportunidades/" + id, null, 409, token);
        requisitar("POST", "/oportunidades/" + id + "/itens",
                Map.of("produtoId", produtoId, "quantidade", 0), 400, token);
    }

    @Test
    void aceitarPropostaFechaOportunidadeERepeticaoNaoMudaValor() {
        long oportunidade = criarOportunidade();
        adicionarItem(oportunidade);
        JsonNode proposta = requisitar("POST", "/propostas/gerar-de-oportunidade/" + oportunidade, null, 201, token);
        long id = proposta.path("id").asLong();
        assertTrue(proposta.path("numero").asText().matches("PROP-[0-9]{4}-[0-9]{6}"));
        assertEquals(LocalDate.now(clock).plusDays(15).toString(), proposta.path("dataVencimento").asText());
        requisitar("PUT", "/propostas/" + id + "/aceitar", null, 409, token);
        requisitar("PUT", "/propostas/" + id + "/enviar", null, 200, token);
        requisitar("PUT", "/propostas/" + id + "/aceitar", null, 200, token);
        JsonNode fechada = requisitar("GET", "/oportunidades/" + oportunidade, null, 200, token);
        assertEquals("GANHA", fechada.path("status").asText());
        assertEquals(25.0, fechada.path("valorReal").asDouble(), 0.001);
        requisitar("PUT", "/propostas/" + id + "/aceitar", null, 409, token);
        requisitar("PUT", "/oportunidades/" + oportunidade + "/mudar-estagio",
                Map.of("estagioId", estagioInicialId), 422, token);
        requisitar("POST", "/oportunidades/" + oportunidade + "/itens",
                Map.of("produtoId", produtoId, "quantidade", 1), 422, token);
    }

    @Test
    void falhaAoSalvarAceiteDesfazTodasAsAlteracoes() {
        long oportunidade = criarOportunidade();
        adicionarItem(oportunidade);
        long proposta = requisitar("POST", "/propostas/gerar-de-oportunidade/" + oportunidade,
                null, 201, token).path("id").asLong();
        requisitar("PUT", "/propostas/" + proposta + "/enviar", null, 200, token);
        jdbc.execute("ALTER TABLE propostas ADD CONSTRAINT teste_bloqueia_aceite CHECK (status <> 'ACEITA')");
        try {
            requisitar("PUT", "/propostas/" + proposta + "/aceitar", null, 409, token);
            assertEquals("ENVIADA", jdbc.queryForObject("SELECT status FROM propostas WHERE id=?", String.class, proposta));
            assertEquals("ATIVA", jdbc.queryForObject("SELECT status FROM oportunidades WHERE id=?", String.class, oportunidade));
            assertNull(jdbc.queryForObject("SELECT data_fechamento_real FROM oportunidades WHERE id=?", Object.class, oportunidade));
        } finally {
            jdbc.execute("ALTER TABLE propostas DROP CONSTRAINT teste_bloqueia_aceite");
        }
    }

    @Test
    void numeracaoConcorrenteNaoGeraDuplicados() throws Exception {
        List<Long> oportunidades = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            long oportunidade = criarOportunidade();
            adicionarItem(oportunidade);
            oportunidades.add(oportunidade);
        }
        try (var executor = Executors.newFixedThreadPool(4)) {
            List<Callable<String>> acoes = new ArrayList<>();
            for (Long oportunidade : oportunidades) {
                acoes.add(() -> requisitar("POST", "/propostas/gerar-de-oportunidade/" + oportunidade,
                        null, 201, token).path("numero").asText());
            }
            var resultados = executor.invokeAll(acoes);
            List<String> numeros = new ArrayList<>();
            for (var resultado : resultados) {
                numeros.add(resultado.get());
            }
            assertEquals(4, numeros.stream().distinct().count());
        }
    }

    @Test
    void urgentesIncluiAtrasadasOrdenaPrioridadeEConclusaoSaiDaConsulta() {
        LocalDate hoje = LocalDate.now(clock);
        criarTarefa("Baixa hoje", hoje, "BAIXA");
        long alta = criarTarefa("Alta hoje", hoje, "ALTA");
        criarTarefa("Atrasada", hoje.minusDays(1), "MEDIA");
        criarTarefa("Futura", hoje.plusDays(1), "ALTA");
        JsonNode urgentes = requisitar("GET", "/tarefas/urgentes?dias=0", null, 200, token);
        assertEquals(3, urgentes.path("page").path("totalElements").asLong());
        assertEquals("Atrasada", urgentes.path("content").get(0).path("titulo").asText());
        assertEquals("Alta hoje", urgentes.path("content").get(1).path("titulo").asText());
        JsonNode concluida = requisitar("PUT", "/tarefas/" + alta + "/concluir", null, 200, token);
        assertFalse(concluida.path("dataConclusao").isNull());
        assertEquals(2, requisitar("GET", "/tarefas/urgentes?dias=0", null, 200, token)
                .path("page").path("totalElements").asLong());
        requisitar("GET", "/tarefas/urgentes?dias=-1", null, 400, token);
    }

    @Test
    void historicoUneOrigensPaginaGlobalmenteEProtegeAutoria() {
        long oportunidade = criarOportunidade();
        JsonNode nota = requisitar("POST", "/notas", Map.of("conteudo", "Nota de teste",
                "usuarioId", usuarioId, "oportunidadeId", oportunidade), 201, token);
        requisitar("POST", "/interacoes", Map.of("tipo", "EMAIL", "descricao", "Contato antigo",
                "dataInteracao", LocalDate.now(clock).minusDays(2) + "T10:00:00",
                "usuarioId", usuarioId, "oportunidadeId", oportunidade), 201, token);
        JsonNode primeira = requisitar("GET", "/oportunidades/" + oportunidade + "/historico?size=1&page=0",
                null, 200, token);
        assertEquals(2, primeira.path("page").path("totalElements").asLong());
        assertEquals("NOTA", primeira.path("content").get(0).path("origem").asText());
        JsonNode segunda = requisitar("GET", "/oportunidades/" + oportunidade + "/historico?size=1&page=1",
                null, 200, token);
        assertEquals("INTERACAO", segunda.path("content").get(0).path("origem").asText());
        requisitar("PUT", "/oportunidades/" + oportunidade, Map.of("titulo", "Troca indevida",
                "empresaId", outraEmpresaId, "vendedorId", usuarioId), 409, token);
        requisitar("POST", "/notas", Map.of("conteudo", "Sem vínculo", "usuarioId", usuarioId), 422, token);
        requisitar("POST", "/notas", Map.of("conteudo", "Autor forjado",
                "usuarioId", outroUsuarioId, "empresaId", empresaId), 403, token);
        requisitar("DELETE", "/notas/" + nota.path("id").asLong(), null, 403, outroToken);
        requisitar("PUT", "/notas/" + nota.path("id").asLong(), Map.of("conteudo", "Edição indevida",
                "usuarioId", outroUsuarioId, "oportunidadeId", oportunidade), 403, outroToken);
    }

    @Test
    void consultaCompletaEExclusoesPreservamVinculos() {
        long oportunidade = criarOportunidade();
        long itemId = adicionarItem(oportunidade);
        JsonNode completa = requisitar("GET", "/oportunidades/" + oportunidade + "/completa", null, 200, token);
        assertEquals(empresaId.longValue(), completa.path("empresa").path("id").asLong());
        assertEquals(1, completa.path("itens").size());
        requisitar("DELETE", "/estagios/" + estagioInicialId, null, 409, token);
        requisitar("DELETE", "/oportunidades/" + oportunidade + "/itens/" + itemId, null, 204, token);
        requisitar("DELETE", "/oportunidades/" + oportunidade, null, 204, token);
        requisitar("GET", "/oportunidades/" + oportunidade, null, 404, token);
    }

    private long criarOportunidade() {
        return requisitar("POST", "/oportunidades", Map.of("titulo", "Negociação teste",
                "empresaId", empresaId, "vendedorId", usuarioId, "valorEstimado", 100), 201, token).path("id").asLong();
    }

    private long adicionarItem(long oportunidade) {
        return requisitar("POST", "/oportunidades/" + oportunidade + "/itens",
                Map.of("produtoId", produtoId, "quantidade", 2), 201, token).path("id").asLong();
    }

    private long criarTarefa(String titulo, LocalDate data, String prioridade) {
        return requisitar("POST", "/tarefas", Map.of("titulo", titulo, "responsavelId", usuarioId,
                "status", "PENDENTE", "prioridade", prioridade, "dataVencimento", data.toString()), 201, token)
                .path("id").asLong();
    }

    private String login(String email) {
        return requisitar("POST", "/auth/login", Map.of("email", email, "senha", "senha-ficticia-integracao"),
                200, null).path("token").asText();
    }

    private JsonNode requisitar(String metodo, String caminho, Object corpo, int esperado, String credencial) {
        try {
            var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + caminho))
                    .header("Content-Type", "application/json");
            if (credencial != null) {
                builder.header("Authorization", "Bearer " + credencial);
            }
            var publisher = corpo == null ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(corpo));
            var resposta = client.send(builder.method(metodo, publisher).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(esperado, resposta.statusCode(), metodo + " " + caminho + ": " + resposta.body());
            return resposta.body().isBlank() ? json.createObjectNode() : json.readTree(resposta.body());
        } catch (Exception exception) {
            throw new AssertionError("Falha na chamada HTTP local", exception);
        }
    }
}
