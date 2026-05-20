package com.sijo.boondmcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sijo.boondmcp.client.PythonBackendClient;
import com.sijo.boondmcp.dto.PingRequest;
import com.sijo.boondmcp.dto.PingResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class PingService {

    private static final Logger log = LoggerFactory.getLogger(PingService.class);
    private static final String PING_PATH = "/ping";
    private static final ParameterizedTypeReference<JsonNode> JSON_NODE_TYPE = new ParameterizedTypeReference<>() {
    };

    private final PythonBackendClient client;

    public PingService(PythonBackendClient client) {
        this.client = client;
    }

    public PingResponse ping(PingRequest request) {
        log.debug("Forwarding ping payload to Python backend: {}", request);
        JsonNode payload = client.post(PING_PATH, request, JSON_NODE_TYPE);
        return new PingResponse(payload);
    }
}