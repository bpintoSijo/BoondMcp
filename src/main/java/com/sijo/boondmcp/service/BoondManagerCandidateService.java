package com.sijo.boondmcp.service;

import com.sijo.boondmcp.client.PythonBackendClient;
import com.sijo.boondmcp.dto.candidate.CandidateDetailDto;
import com.sijo.boondmcp.dto.candidate.CandidateSearchRequestDto;
import com.sijo.boondmcp.dto.candidate.CandidateSearchResponseDto;
import com.sijo.boondmcp.dto.candidate.TechnicalDocumentDto;
import com.sijo.boondmcp.dto.dictionary.DictionaryResponseDto;
import com.sijo.boondmcp.exception.PythonBackendException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.util.function.Consumer;

@Service
public class BoondManagerCandidateService {

    private static final ParameterizedTypeReference<DictionaryResponseDto> DICTIONARY_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<CandidateSearchResponseDto> SEARCH_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<CandidateDetailDto> DETAIL_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<TechnicalDocumentDto> TD_TYPE =
            new ParameterizedTypeReference<>() {};

    private final PythonBackendClient pythonBackendClient;

    public BoondManagerCandidateService(PythonBackendClient pythonBackendClient) {
        this.pythonBackendClient = pythonBackendClient;
    }

    public DictionaryResponseDto getDictionary() {
        return pythonBackendClient.get("/api/dictionary", DICTIONARY_TYPE);
    }

    public CandidateSearchResponseDto searchCandidates(CandidateSearchRequestDto request) {
        Consumer<UriBuilder> queryParams = builder -> {
            addIfPresent(builder, "keywords", request.keywords());
            addIfPresent(builder, "state", request.state());
            addIfPresent(builder, "availabilityType", request.availabilityType());
            addIfPresent(builder, "availabilityDate", request.availabilityDate());
            addIfPresent(builder, "contractType", request.contractType());
            addIfPresent(builder, "experience", request.experience());
            addIfPresent(builder, "training", request.training());
            addIfPresent(builder, "expertiseAreas", request.expertiseAreas());
            addIfPresent(builder, "activityAreas", request.activityAreas());
            addIfPresent(builder, "mobilityArea", request.mobilityArea());
            addIfPresent(builder, "minSalary", request.minSalary());
            addIfPresent(builder, "maxSalary", request.maxSalary());
            addIfPresent(builder, "minTjm", request.minTjm());
            addIfPresent(builder, "maxTjm", request.maxTjm());
            addIfPresent(builder, "page", request.page());
            addIfPresent(builder, "numberPerPage", request.numberPerPage());
        };
        return pythonBackendClient.get("/api/candidates", queryParams, SEARCH_TYPE);
    }

    public CandidateDetailDto getCandidateDetail(Integer candidateId) {
        try {
            return pythonBackendClient.get("/api/candidates/" + candidateId, DETAIL_TYPE);
        } catch (PythonBackendException ex) {
            throw mapNotFound(ex, "Candidate not found in BoondManager: " + candidateId);
        }
    }

    public TechnicalDocumentDto getCandidateTechnicalDocument(Integer candidateId) {
        try {
            return pythonBackendClient.get(
                    "/api/candidates/" + candidateId + "/technical-document", TD_TYPE);
        } catch (PythonBackendException ex) {
            throw mapNotFound(ex,
                    "Technical document not found for candidate: " + candidateId);
        }
    }

    private static void addIfPresent(UriBuilder builder, String name, Object value) {
        if (value != null) {
            builder.queryParam(name, value);
        }
    }

    private static RuntimeException mapNotFound(PythonBackendException ex, String message) {
        Throwable cause = ex.getCause();
        if (cause instanceof WebClientResponseException wcre) {
            HttpStatusCode status = wcre.getStatusCode();
            if (status.value() == HttpStatus.NOT_FOUND.value()) {
                return new RuntimeException(message, ex);
            }
        }
        return ex;
    }
}