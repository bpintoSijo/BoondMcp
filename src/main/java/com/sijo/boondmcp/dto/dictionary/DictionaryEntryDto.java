package com.sijo.boondmcp.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DictionaryEntryDto(
        String id,
        String label
) {
}