package com.sijo.boondmcp.audit;

import com.sijo.boondmcp.infrastructure.MdcKeys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class ToolAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(ToolAuditAspect.class);

    private final ToolAuditLogger auditLogger;

    public ToolAuditAspect(ToolAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Around("@annotation(toolAnnotation)")
    public Object aroundTool(ProceedingJoinPoint pjp, Tool toolAnnotation) throws Throwable {
        String toolName = toolAnnotation.name() == null || toolAnnotation.name().isBlank()
                ? pjp.getSignature().getName()
                : toolAnnotation.name();
        String correlationId = MDC.get(MdcKeys.CORRELATION_ID);
        Instant start = Instant.now();

        MDC.put(MdcKeys.TOOL_NAME, toolName);
        auditLogger.logStart(toolName, correlationId);
        log.info("Executing tool '{}'", toolName);

        try {
            Object result = pjp.proceed();
            Duration duration = Duration.between(start, Instant.now());
            auditLogger.logSuccess(toolName, correlationId, duration);
            log.info("Tool '{}' executed in {} ms", toolName, duration.toMillis());
            return result;
        } catch (Throwable ex) {
            Duration duration = Duration.between(start, Instant.now());
            auditLogger.logFailure(toolName, correlationId, duration, ex);
            log.error("Tool '{}' failed after {} ms", toolName, duration.toMillis(), ex);
            throw ex;
        } finally {
            MDC.remove(MdcKeys.TOOL_NAME);
        }
    }
}