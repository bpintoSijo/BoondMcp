package com.sijo.boondmcp.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DictionaryOptionEntryDto(
        OptionId option,
        String label
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OptionId(String id) {
    }
}