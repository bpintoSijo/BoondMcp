package com.sijo.mcpboondmanager.dto.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DictionaryEntryDto(
        String id,
        String label
) {
}