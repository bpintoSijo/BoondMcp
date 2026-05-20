package com.sijo.boondmcp.dto.dictionary;

import java.util.List;

public record DictionarySnapshot(
        List<DictionaryEntry> expertiseArea,
        List<DictionaryEntry> tool,
        List<DictionaryEntry> activityArea,
        List<DictionaryEntry> languageSpoken,
        List<DictionaryEntry> languageLevel,
        List<DictionaryEntry> contractType,
        List<DictionaryEntry> candidateState,
        List<DictionaryEntry> evaluation,
        List<DictionaryEntry> availability
) {

    public DictionarySnapshot {
        expertiseArea = nullToEmpty(expertiseArea);
        tool = nullToEmpty(tool);
        activityArea = nullToEmpty(activityArea);
        languageSpoken = nullToEmpty(languageSpoken);
        languageLevel = nullToEmpty(languageLevel);
        contractType = nullToEmpty(contractType);
        candidateState = nullToEmpty(candidateState);
        evaluation = nullToEmpty(evaluation);
        availability = nullToEmpty(availability);
    }

    private static List<DictionaryEntry> nullToEmpty(List<DictionaryEntry> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}