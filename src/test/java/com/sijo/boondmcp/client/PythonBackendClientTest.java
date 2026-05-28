package com.sijo.boondmcp.client;

import com.sijo.boondmcp.config.PythonBackendProperties;
import com.sijo.boondmcp.config.WebClientConfig;
import com.sijo.boondmcp.dto.candidate.CandidateDetailDto;
import com.sijo.boondmcp.dto.candidate.CandidateSearchResponseDto;
import com.sijo.boondmcp.dto.candidate.TechnicalDocumentDto;
import com.sijo.boondmcp.dto.dictionary.DictionaryResponseDto;
import com.sijo.boondmcp.exception.PythonBackendException;
import com.sijo.boondmcp.infrastructure.CorrelationIdFilter;
import com.sijo.boondmcp.infrastructure.MdcKeys;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class PythonBackendClientTest {

    private static final ParameterizedTypeReference<DictionaryResponseDto> DICTIONARY_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<CandidateSearchResponseDto> SEARCH_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<CandidateDetailDto> DETAIL_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<TechnicalDocumentDto> TD_TYPE =
            new ParameterizedTypeReference<>() {};

    private HttpServer server;
    private ExecutorService executor;
    private AtomicReference<CapturedRequest> capturedRequest;

    @BeforeEach
    void setUp() throws IOException {
        capturedRequest = new AtomicReference<>();
        executor = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(executor);
        server.start();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    void givenDictionaryEndpoint_whenGet_thenDeserializesResponseAndPropagatesCorrelationId() {
        respondJson("/api/dictionary", 200, """
                {
                  "setting": {
                    "state": {"candidate": [{"id": "1", "label": "Active"}]},
                    "availability": [{"id": "9", "label": "Available"}],
                    "mobilityArea": [{"option": {"id": "idf"}, "label": "Ile-de-France"}]
                  }
                }
                """);
        MDC.put(MdcKeys.CORRELATION_ID, "corr-123");

        DictionaryResponseDto response = client(Duration.ofSeconds(2))
                .get("/api/dictionary", DICTIONARY_TYPE);

        assertThat(response.setting().state().candidate())
                .extracting("id", "label")
                .containsExactly(tuple("1", "Active"));
        assertThat(capturedRequest.get().method()).isEqualTo("GET");
        assertThat(capturedRequest.get().path()).isEqualTo("/api/dictionary");
        assertThat(capturedRequest.get().header(CorrelationIdFilter.HEADER_NAME)).isEqualTo("corr-123");
    }

    @Test
    void givenCandidateSearchEndpoint_whenPost_thenSerializesRequestAndDeserializesResponse() {
        respondJson("/api/candidates/search", 200, """
                {
                  "candidates": [
                    {
                      "id": 42,
                      "firstName": "Ada",
                      "lastName": "Lovelace",
                      "technicalDocument": {"title": "Senior Java Engineer"}
                    }
                  ],
                  "meta": {"totalRows": 1, "currentPage": 1}
                }
                """);

        CandidateSearchResponseDto response = client(Duration.ofSeconds(2))
                .post("/api/candidates/search", Map.of("keywords", "java"), SEARCH_TYPE);

        assertThat(response.candidates()).hasSize(1);
        assertThat(response.candidates().getFirst().id()).isEqualTo(42);
        assertThat(response.candidates().getFirst().technicalDocument().title())
                .isEqualTo("Senior Java Engineer");
        assertThat(response.meta().totalRows()).isEqualTo(1);
        assertThat(capturedRequest.get().method()).isEqualTo("POST");
        assertThat(capturedRequest.get().body()).contains("\"keywords\":\"java\"");
    }

    @Test
    void givenCandidateDetailEndpoint_whenGet_thenDeserializesResponse() {
        respondJson("/api/candidates/42", 200, """
                {
                  "id": 42,
                  "firstName": "Ada",
                  "lastName": "Lovelace",
                  "email": "ada@example.test",
                  "technicalDocumentId": 101
                }
                """);

        CandidateDetailDto response = client(Duration.ofSeconds(2))
                .get("/api/candidates/42", DETAIL_TYPE);

        assertThat(response.id()).isEqualTo(42);
        assertThat(response.email()).isEqualTo("ada@example.test");
        assertThat(response.technicalDocumentId()).isEqualTo(101);
        assertThat(capturedRequest.get().path()).isEqualTo("/api/candidates/42");
    }

    @Test
    void givenTechnicalDocumentEndpoint_whenGet_thenDeserializesResponse() {
        respondJson("/api/candidates/42/technical-document", 200, """
                {
                  "id": 101,
                  "title": "Senior Java Engineer",
                  "skills": "Java, Spring",
                  "candidateId": 42
                }
                """);

        TechnicalDocumentDto response = client(Duration.ofSeconds(2))
                .get("/api/candidates/42/technical-document", TD_TYPE);

        assertThat(response.id()).isEqualTo(101);
        assertThat(response.skills()).isEqualTo("Java, Spring");
        assertThat(response.candidateId()).isEqualTo(42);
    }

    @Test
    void givenBackend400_whenGet_thenMapsToPythonBackendException() {
        respondJson("/bad-request", 400, "{\"error\":\"bad input\"}");

        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).get("/bad-request", DICTIONARY_TYPE))
                .isInstanceOf(PythonBackendException.class)
                .hasMessageContaining("Python backend returned 400 BAD_REQUEST")
                .hasMessageContaining("GET /bad-request");
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403})
    void givenUnauthorizedOrForbiddenBackendResponse_whenGet_thenMapsToSafePythonBackendException(int status) {
        respondJson("/secure", status, "{\"token\":\"secret-token-value\"}");

        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).get("/secure", DICTIONARY_TYPE))
                .isInstanceOf(PythonBackendException.class)
                .hasMessageContaining("GET /secure")
                .hasMessageNotContaining("secret-token-value");
    }

    @Test
    void givenBackend404_whenGet_thenMapsToPythonBackendException() {
        respondJson("/missing", 404, "{\"message\":\"not found\"}");

        assertThatThrownBy(() -> client(Duration.ofSeconds(2)).get("/missing", DICTIONARY_TYPE))
                .isInstanceOf(PythonBackendException.class)
                .hasMessageContaining("404 NOT_FOUND")
                .hasMessageContaining("GET /missing");
    }

    @Test
    void givenBackend500_whenPost_thenMapsToPythonBackendException() {
        respondJson("/api/candidates/search", 500, "{\"message\":\"backend down\"}");

        assertThatThrownBy(() -> client(Duration.ofSeconds(2))
                .post("/api/candidates/search", Map.of("keywords", "java"), SEARCH_TYPE))
                .isInstanceOf(PythonBackendException.class)
                .hasMessageContaining("500 INTERNAL_SERVER_ERROR")
                .hasMessageContaining("POST /api/candidates/search");
    }

    @Test
    void givenSlowBackend_whenGet_thenTimeoutMapsToPythonBackendException() {
        server.createContext("/slow", exchange -> {
            capturedRequest.set(capture(exchange));
            try {
                Thread.sleep(1_000);
                writeJson(exchange, 200, "{}");
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                // The client is expected to close the exchange after the configured timeout.
            }
        });

        assertThatThrownBy(() -> client(Duration.ofMillis(100)).get("/slow", DICTIONARY_TYPE))
                .isInstanceOf(PythonBackendException.class)
                .hasMessageContaining("GET /slow");
    }

    @Test
    void givenBaseUrlFromProperties_whenGet_thenUsesConfiguredServer() {
        respondJson("/base-url-check", 200, "{\"setting\": {}}");
        PythonBackendProperties properties = properties(Duration.ofSeconds(2));

        DictionaryResponseDto response = new PythonBackendClient(
                new WebClientConfig().pythonBackendWebClient(properties),
                properties
        ).get("/base-url-check", DICTIONARY_TYPE);

        assertThat(response.setting()).isNotNull();
        assertThat(capturedRequest.get().path()).isEqualTo("/base-url-check");
    }

    private PythonBackendClient client(Duration timeout) {
        PythonBackendProperties properties = properties(timeout);
        return new PythonBackendClient(new WebClientConfig().pythonBackendWebClient(properties), properties);
    }

    private PythonBackendProperties properties(Duration timeout) {
        return new PythonBackendProperties("http://localhost:" + server.getAddress().getPort(), timeout);
    }

    private void respondJson(String path, int status, String body) {
        server.createContext(path, exchange -> {
            capturedRequest.set(capture(exchange));
            writeJson(exchange, status, body);
        });
    }

    private CapturedRequest capture(HttpExchange exchange) throws IOException {
        return new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestURI().getPath(),
                exchange.getRequestURI().getRawQuery(),
                exchange.getRequestHeaders().getFirst(CorrelationIdFilter.HEADER_NAME),
                new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)
        );
    }

    private void writeJson(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private record CapturedRequest(
            String method,
            String path,
            String query,
            String correlationId,
            String body
    ) {
        String header(String name) {
            if (CorrelationIdFilter.HEADER_NAME.equalsIgnoreCase(name)) {
                return correlationId;
            }
            return null;
        }
    }
}
