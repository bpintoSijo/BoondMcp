package com.sijo.boondmcp.dto.candidate;

public record CandidateSearchRequestDto(
        String keywords,
        Integer state,
        Integer availabilityType,
        String availabilityDate,
        Integer contractType,
        Integer experience,
        String training,
        String expertiseAreas,
        String activityAreas,
        String mobilityArea,
        Double minSalary,
        Double maxSalary,
        Double minTjm,
        Double maxTjm,
        Integer page,
        Integer numberPerPage
) {
}