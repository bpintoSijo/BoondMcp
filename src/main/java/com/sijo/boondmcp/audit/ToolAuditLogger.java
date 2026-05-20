package com.sijo.boondmcp.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ToolAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("audit.tool");

    public void logStart(String toolName, String correlationId) {
        log.info("tool_start tool={} correlationId={}", toolName, correlationId);
    }

    public void logSuccess(String toolName, String correlationId, Duration duration) {
        log.info("tool_success tool={} correlationId={} durationMs={}",
                toolName, correlationId, duration.toMillis());
    }

    public void logFailure(String toolName, String correlationId, Duration duration, Throwable error) {
        log.warn("tool_failure tool={} correlationId={} durationMs={} error={} message={}",
                toolName, correlationId, duration.toMillis(),
                error.getClass().getSimpleName(), error.getMessage());
    }
}