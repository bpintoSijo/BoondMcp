package com.sijo.boondmcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "mcp.python")
public record PythonBackendProperties(
        String baseUrl,
        Duration timeout
) {
}