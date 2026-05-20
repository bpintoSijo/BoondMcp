package com.sijo.boondmcp.client;

import com.sijo.boondmcp.config.PythonBackendProperties;
import com.sijo.boondmcp.exception.PythonBackendException;
import com.sijo.boondmcp.infrastructure.CorrelationIdFilter;
import com.sijo.boondmcp.infrastructure.MdcKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class PythonBackendClient {

    private static final Logger log = LoggerFactory.getLogger(PythonBackendClient.class);

    private final WebClient webClient;
    private final PythonBackendProperties properties;

    public PythonBackendClient(WebClient pythonBackendWebClient, PythonBackendProperties properties) {
        this.webClient = pythonBackendWebClient;
        this.properties = properties;
    }

    public <T> T post(String path, Object body, ParameterizedTypeReference<T> responseType) {
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.debug("Calling Python backend POST {}{}", properties.baseUrl(), path);
        try {
            return webClient.post()
                    .uri(path)
                    .header(CorrelationIdFilter.HEADER_NAME, correlationId == null ? "" : correlationId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("Python backend returned error {} on POST {} : {}",
                    ex.getStatusCode(), path, ex.getResponseBodyAsString());
            throw new PythonBackendException(
                    "Python backend returned " + ex.getStatusCode() + " on " + path, ex);
        } catch (WebClientRequestException ex) {
            log.error("Failed to reach Python backend on POST {}", path, ex);
            throw new PythonBackendException("Unable to reach Python backend on " + path, ex);
        } catch (RuntimeException ex) {
            log.error("Unexpected error while calling Python backend on POST {}", path, ex);
            throw new PythonBackendException("Unexpected error while calling Python backend on " + path, ex);
        }
    }

    public <T> T get(String path, ParameterizedTypeReference<T> responseType) {
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        log.debug("Calling Python backend GET {}{}", properties.baseUrl(), path);
        try {
            return webClient.get()
                    .uri(path)
                    .header(CorrelationIdFilter.HEADER_NAME, correlationId == null ? "" : correlationId)
                    .retrieve()
                    .bodyToMono(responseType)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("Python backend returned error {} on GET {} : {}",
                    ex.getStatusCode(), path, ex.getResponseBodyAsString());
            throw new PythonBackendException(
                    "Python backend returned " + ex.getStatusCode() + " on " + path, ex);
        } catch (WebClientRequestException ex) {
            log.error("Failed to reach Python backend on GET {}", path, ex);
            throw new PythonBackendException("Unable to reach Python backend on " + path, ex);
        } catch (RuntimeException ex) {
            log.error("Unexpected error while calling Python backend on GET {}", path, ex);
            throw new PythonBackendException("Unexpected error while calling Python backend on " + path, ex);
        }
    }
}