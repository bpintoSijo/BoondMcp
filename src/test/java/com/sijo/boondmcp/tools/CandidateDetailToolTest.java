package com.sijo.boondmcp.tools;

import com.sijo.boondmcp.client.PythonBackendClient;
import com.sijo.boondmcp.dto.candidate.CandidateDetailDto;
import com.sijo.boondmcp.exception.PythonBackendException;
import com.sijo.boondmcp.service.BoondManagerCandidateService;
import com.sijo.boondmcp.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.tool.annotation.Tool;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidateDetailToolTest {

    @Mock
    private BoondManagerCandidateService candidateService;

    @Test
    void givenCandidateId_whenGetCandidateDetail_thenReturnsServiceResponse() {
        CandidateDetailDto expected = TestFixtures.candidateDetail();
        when(candidateService.getCandidateDetail(42)).thenReturn(expected);

        CandidateDetailDto response = tool().getCandidateDetail(42);

        assertThat(response).isSameAs(expected);
        verify(candidateService).getCandidateDetail(42);
        verifyNoMoreInteractions(candidateService);
    }

    @Test
    void givenNullCandidateId_whenGetCandidateDetail_thenDelegatesCurrentServiceBehavior() {
        CandidateDetailDto expected = TestFixtures.candidateDetail();
        when(candidateService.getCandidateDetail(null)).thenReturn(expected);

        CandidateDetailDto response = tool().getCandidateDetail(null);

        assertThat(response).isSameAs(expected);
        verify(candidateService).getCandidateDetail(null);
    }

    @Test
    void givenNotFoundException_whenGetCandidateDetail_thenPropagatesException() {
        RuntimeException exception = new RuntimeException("Candidate not found in BoondManager: 42");
        when(candidateService.getCandidateDetail(42)).thenThrow(exception);

        assertThatThrownBy(() -> tool().getCandidateDetail(42))
                .isSameAs(exception);
    }

    @Test
    void givenBackendException_whenGetCandidateDetail_thenPropagatesProjectException() {
        PythonBackendException exception = new PythonBackendException("backend failed", new RuntimeException());
        when(candidateService.getCandidateDetail(42)).thenThrow(exception);

        assertThatThrownBy(() -> tool().getCandidateDetail(42))
                .isSameAs(exception);
    }

    @Test
    void givenToolClass_whenInspected_thenToolNameRemainsUnchanged() throws NoSuchMethodException {
        Tool annotation = CandidateDetailTool.class
                .getMethod("getCandidateDetail", Integer.class)
                .getAnnotation(Tool.class);

        assertThat(annotation.name()).isEqualTo("getCandidateDetail");
    }

    @Test
    void givenToolClass_whenInspected_thenDoesNotDependOnPythonBackendClientDirectly() {
        assertThat(Arrays.stream(CandidateDetailTool.class.getDeclaredFields()).map(Field::getType))
                .contains(BoondManagerCandidateService.class)
                .doesNotContain(PythonBackendClient.class);
    }

    private CandidateDetailTool tool() {
        return new CandidateDetailTool(candidateService);
    }
}
