package com.sijo.boondmcp.audit;

import com.sijo.boondmcp.infrastructure.MdcKeys;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.ai.tool.annotation.Tool;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ToolAuditAspectTest {

    @Mock
    private ToolAuditLogger auditLogger;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private Tool toolAnnotation;

    private ToolAuditAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new ToolAuditAspect(auditLogger);
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void givenSuccessfulToolCall_whenAroundTool_thenLogsStartedAndCompletedWithLatency() throws Throwable {
        MDC.put(MdcKeys.CORRELATION_ID, "corr-123");
        when(toolAnnotation.name()).thenReturn("searchCandidates");
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.aroundTool(joinPoint, toolAnnotation);

        assertThat(result).isEqualTo("ok");
        InOrder inOrder = inOrder(auditLogger);
        inOrder.verify(auditLogger).logStart("searchCandidates", "corr-123");

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        inOrder.verify(auditLogger).logSuccess(eq("searchCandidates"), eq("corr-123"), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isGreaterThanOrEqualTo(Duration.ZERO);
        assertThat(MDC.get(MdcKeys.TOOL_NAME)).isNull();
    }

    @Test
    void givenFailedToolCall_whenAroundTool_thenLogsFailureWithLatencyAndRethrows() throws Throwable {
        RuntimeException exception = new RuntimeException("backend failed");
        MDC.put(MdcKeys.CORRELATION_ID, "corr-456");
        when(toolAnnotation.name()).thenReturn("getCandidateDetail");
        when(joinPoint.proceed()).thenThrow(exception);

        assertThatThrownBy(() -> aspect.aroundTool(joinPoint, toolAnnotation))
                .isSameAs(exception);

        InOrder inOrder = inOrder(auditLogger);
        inOrder.verify(auditLogger).logStart("getCandidateDetail", "corr-456");

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        inOrder.verify(auditLogger).logFailure(
                eq("getCandidateDetail"),
                eq("corr-456"),
                durationCaptor.capture(),
                same(exception)
        );
        assertThat(durationCaptor.getValue()).isGreaterThanOrEqualTo(Duration.ZERO);
        assertThat(MDC.get(MdcKeys.TOOL_NAME)).isNull();
    }

    @Test
    void givenBlankAnnotationName_whenAroundTool_thenUsesMethodNameAsToolName() throws Throwable {
        when(toolAnnotation.name()).thenReturn("");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("fallbackToolName");
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.aroundTool(joinPoint, toolAnnotation);

        verify(auditLogger).logStart("fallbackToolName", null);
        verify(auditLogger).logSuccess(eq("fallbackToolName"), eq(null), any(Duration.class));
    }

    @Test
    void givenToolCallHasArguments_whenAroundTool_thenDoesNotReadPayloadForAuditLogging() throws Throwable {
        when(toolAnnotation.name()).thenReturn("searchCandidates");
        when(joinPoint.proceed()).thenReturn("ok");

        aspect.aroundTool(joinPoint, toolAnnotation);

        verify(joinPoint, never()).getArgs();
    }
}
