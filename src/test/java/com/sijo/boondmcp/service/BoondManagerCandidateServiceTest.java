package com.sijo.boondmcp.service;

import com.sijo.boondmcp.client.PythonBackendClient;
import com.sijo.boondmcp.dto.candidate.CandidateDetailDto;
import com.sijo.boondmcp.dto.candidate.CandidateSearchRequestDto;
import com.sijo.boondmcp.dto.candidate.CandidateSearchResponseDto;
import com.sijo.boondmcp.dto.candidate.TechnicalDocumentDto;
import com.sijo.boondmcp.dto.dictionary.DictionaryResponseDto;
import com.sijo.boondmcp.exception.PythonBackendException;
import com.sijo.boondmcp.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoondManagerCandidateServiceTest {

    @Mock
    private PythonBackendClient pythonBackendClient;

    @Test
    void givenDictionaryRequest_whenGetDictionary_thenDelegatesToPythonBackendClient() {
        DictionaryResponseDto expected = TestFixtures.dictionary();
        when(pythonBackendClient.get(eq("/api/dictionary"), any(ParameterizedTypeReference.class)))
                .thenReturn(expected);

        DictionaryResponseDto response = service().getDictionary();

        assertThat(response).isSameAs(expected);
        verify(pythonBackendClient).get(eq("/api/dictionary"), any(ParameterizedTypeReference.class));
    }

    @Test
    void givenCandidateSearchRequest_whenSearchCandidates_thenDelegatesWithQueryParameters() {
        CandidateSearchRequestDto request = TestFixtures.searchRequest();
        CandidateSearchResponseDto expected = TestFixtures.searchResponse();
        when(pythonBackendClient.get(eq("/api/candidates"), any(Consumer.class), any(ParameterizedTypeReference.class)))
                .thenReturn(expected);

        CandidateSearchResponseDto response = service().searchCandidates(request);

        assertThat(response).isSameAs(expected);
        ArgumentCaptor<Consumer<UriBuilder>> queryCaptor = ArgumentCaptor.captor();
        verify(pythonBackendClient).get(
                eq("/api/candidates"),
                queryCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        queryCaptor.getValue().accept(builder);

        assertThat(builder.build().getQueryParams())
                .containsEntry("keywords", List.of("java"))
                .containsEntry("state", List.of("1"))
                .containsEntry("availabilityType", List.of("9"))
                .containsEntry("availabilityDate", List.of("2026-06-01"))
                .containsEntry("contractType", List.of("2"))
                .containsEntry("experience", List.of("3"))
                .containsEntry("training", List.of("bac5"))
                .containsEntry("expertiseAreas", List.of("backend|microservices"))
                .containsEntry("activityAreas", List.of("finance|industry"))
                .containsEntry("mobilityArea", List.of("idf"))
                .containsEntry("minSalary", List.of("40000.0"))
                .containsEntry("maxSalary", List.of("60000.0"))
                .containsEntry("minTjm", List.of("500.0"))
                .containsEntry("maxTjm", List.of("700.0"))
                .containsEntry("page", List.of("1"))
                .containsEntry("numberPerPage", List.of("25"));
    }

    @Test
    void givenCandidateSearchRequestWithNullFilters_whenSearchCandidates_thenOmitsNullQueryParameters() {
        CandidateSearchRequestDto request = new CandidateSearchRequestDto(
                "java",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                25
        );
        when(pythonBackendClient.get(eq("/api/candidates"), any(Consumer.class), any(ParameterizedTypeReference.class)))
                .thenReturn(TestFixtures.searchResponse());

        service().searchCandidates(request);

        ArgumentCaptor<Consumer<UriBuilder>> queryCaptor = ArgumentCaptor.captor();
        verify(pythonBackendClient).get(
                eq("/api/candidates"),
                queryCaptor.capture(),
                any(ParameterizedTypeReference.class)
        );

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        queryCaptor.getValue().accept(builder);
        assertThat(builder.build().getQueryParams().keySet())
                .containsExactlyInAnyOrder("keywords", "page", "numberPerPage");
    }

    @Test
    void givenCandidateId_whenGetCandidateDetail_thenDelegatesToPythonBackendClient() {
        CandidateDetailDto expected = TestFixtures.candidateDetail();
        when(pythonBackendClient.get(eq("/api/candidates/42"), any(ParameterizedTypeReference.class)))
                .thenReturn(expected);

        CandidateDetailDto response = service().getCandidateDetail(42);

        assertThat(response).isSameAs(expected);
        verify(pythonBackendClient).get(eq("/api/candidates/42"), any(ParameterizedTypeReference.class));
    }

    @Test
    void givenCandidateId_whenGetTechnicalDocument_thenDelegatesToPythonBackendClient() {
        TechnicalDocumentDto expected = TestFixtures.technicalDocument();
        when(pythonBackendClient.get(
                eq("/api/candidates/42/technical-document"),
                any(ParameterizedTypeReference.class))
        ).thenReturn(expected);

        TechnicalDocumentDto response = service().getCandidateTechnicalDocument(42);

        assertThat(response).isSameAs(expected);
        verify(pythonBackendClient).get(
                eq("/api/candidates/42/technical-document"),
                any(ParameterizedTypeReference.class)
        );
    }

    @Test
    void givenBackendException_whenSearchCandidates_thenPropagatesException() {
        PythonBackendException exception = new PythonBackendException("backend failed", new RuntimeException());
        when(pythonBackendClient.get(eq("/api/candidates"), any(Consumer.class), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);
        BoondManagerCandidateService service = service();
        assertThatThrownBy(() -> service.searchCandidates(TestFixtures.searchRequest()))
                .isSameAs(exception);
    }

    @Test
    void givenCandidateNotFound_whenGetCandidateDetail_thenNormalizesToRuntimeException() {
        PythonBackendException exception = notFoundException();
        when(pythonBackendClient.get(eq("/api/candidates/404"), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);

        BoondManagerCandidateService service = service();
        assertThatThrownBy(() -> service.getCandidateDetail(404))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Candidate not found in BoondManager: 404")
                .hasCause(exception);
    }

    @Test
    void givenTechnicalDocumentNotFound_whenGetTechnicalDocument_thenNormalizesToRuntimeException() {
        PythonBackendException exception = notFoundException();
        when(pythonBackendClient.get(
                eq("/api/candidates/404/technical-document"),
                any(ParameterizedTypeReference.class))
        ).thenThrow(exception);

        BoondManagerCandidateService service = service();
        assertThatThrownBy(() -> service.getCandidateTechnicalDocument(404))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Technical document not found for candidate: 404")
                .hasCause(exception);
    }

    @Test
    void givenNon404BackendException_whenGetCandidateDetail_thenPropagatesOriginalException() {
        PythonBackendException exception = new PythonBackendException(
                "backend failed",
                WebClientResponseException.create(
                        500,
                        "Internal Server Error",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                )
        );
        when(pythonBackendClient.get(eq("/api/candidates/42"), any(ParameterizedTypeReference.class)))
                .thenThrow(exception);

        BoondManagerCandidateService service = service();
        assertThatThrownBy(() -> service.getCandidateDetail(42))
                .isSameAs(exception);
    }

    @Test
    void givenNullCandidateId_whenGetCandidateDetail_thenUsesCurrentDelegationBehavior() {
        CandidateDetailDto expected = TestFixtures.candidateDetail();
        when(pythonBackendClient.get(eq("/api/candidates/null"), any(ParameterizedTypeReference.class)))
                .thenReturn(expected);

        CandidateDetailDto response = service().getCandidateDetail(null);

        assertThat(response).isSameAs(expected);
        verify(pythonBackendClient).get(eq("/api/candidates/null"), any(ParameterizedTypeReference.class));
    }

    private BoondManagerCandidateService service() {
        return new BoondManagerCandidateService(pythonBackendClient);
    }

    private PythonBackendException notFoundException() {
        return new PythonBackendException(
                "not found",
                WebClientResponseException.create(
                        404,
                        "Not Found",
                        HttpHeaders.EMPTY,
                        new byte[0],
                        StandardCharsets.UTF_8
                )
        );
    }
}
