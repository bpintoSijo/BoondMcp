package com.sijo.boondmcp.dto.dictionary;

public record CandidateStatusDTO(
        String id,
        String label,
        CandidateStatusKind kind
) {
}