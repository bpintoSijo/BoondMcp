package com.sijo.boondmcp.dto.dictionary;

import java.util.List;

public record CandidateStatusesResponse(
        List<CandidateStatusDTO> states,
        List<CandidateStatusDTO> evaluations
) {
}