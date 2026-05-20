package com.sijo.boondmcp.dto.dictionary;

import java.util.List;

public record LanguagesResponse(
        List<LanguageDTO> languages,
        List<LanguageLevelDTO> levels
) {
}