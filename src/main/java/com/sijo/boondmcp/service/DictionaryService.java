package com.sijo.boondmcp.service;

import com.sijo.boondmcp.client.PythonBackendClient;
import com.sijo.boondmcp.dto.dictionary.AvailabilityStatusDTO;
import com.sijo.boondmcp.dto.dictionary.CandidateStatusDTO;
import com.sijo.boondmcp.dto.dictionary.CandidateStatusKind;
import com.sijo.boondmcp.dto.dictionary.CandidateStatusesResponse;
import com.sijo.boondmcp.dto.dictionary.ContractTypeDTO;
import com.sijo.boondmcp.dto.dictionary.DictionaryEntry;
import com.sijo.boondmcp.dto.dictionary.DictionarySnapshot;
import com.sijo.boondmcp.dto.dictionary.LanguageDTO;
import com.sijo.boondmcp.dto.dictionary.LanguageLevelDTO;
import com.sijo.boondmcp.dto.dictionary.LanguagesResponse;
import com.sijo.boondmcp.dto.dictionary.SkillDTO;
import com.sijo.boondmcp.dto.dictionary.SkillType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private static final String DICTIONARY_PATH = "/dictionary";
    private static final ParameterizedTypeReference<DictionarySnapshot> SNAPSHOT_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final PythonBackendClient client;

    public DictionaryService(PythonBackendClient client) {
        this.client = client;
    }

    public List<SkillDTO> listSkills() {
        log.info("dictionary_access type=skills");
        DictionarySnapshot snapshot = fetchSnapshot();
        List<SkillDTO> skills = new ArrayList<>(
                snapshot.expertiseArea().size()
                        + snapshot.tool().size()
                        + snapshot.activityArea().size());
        appendSkills(skills, snapshot.expertiseArea(), SkillType.SKILL);
        appendSkills(skills, snapshot.tool(), SkillType.TOOL);
        appendSkills(skills, snapshot.activityArea(), SkillType.ACTIVITY_AREA);
        return List.copyOf(skills);
    }

    public List<SkillDTO> searchSkills(String query) {
        log.info("dictionary_access type=skills_search query='{}'", query);
        if (query == null || query.isBlank()) {
            return listSkills();
        }
        String needle = query.toLowerCase(Locale.ROOT);
        return listSkills().stream()
                .filter(s -> s.label() != null && s.label().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    public LanguagesResponse listLanguages() {
        log.info("dictionary_access type=languages");
        DictionarySnapshot snapshot = fetchSnapshot();
        List<LanguageDTO> languages = map(snapshot.languageSpoken(), e -> new LanguageDTO(e.id(), e.label()));
        List<LanguageLevelDTO> levels = map(snapshot.languageLevel(), e -> new LanguageLevelDTO(e.id(), e.label()));
        return new LanguagesResponse(languages, levels);
    }

    public List<ContractTypeDTO> listContractTypes() {
        log.info("dictionary_access type=contract_types");
        return map(fetchSnapshot().contractType(), e -> new ContractTypeDTO(e.id(), e.label()));
    }

    public CandidateStatusesResponse listCandidateStatuses() {
        log.info("dictionary_access type=candidate_statuses");
        DictionarySnapshot snapshot = fetchSnapshot();
        List<CandidateStatusDTO> states = map(snapshot.candidateState(),
                e -> new CandidateStatusDTO(e.id(), e.label(), CandidateStatusKind.STATE));
        List<CandidateStatusDTO> evaluations = map(snapshot.evaluation(),
                e -> new CandidateStatusDTO(e.id(), e.label(), CandidateStatusKind.EVALUATION));
        return new CandidateStatusesResponse(states, evaluations);
    }

    public List<AvailabilityStatusDTO> listAvailabilityStatuses() {
        log.info("dictionary_access type=availability_statuses");
        return map(fetchSnapshot().availability(), e -> new AvailabilityStatusDTO(e.id(), e.label()));
    }

    private DictionarySnapshot fetchSnapshot() {
        log.debug("Fetching dictionary snapshot from Python backend at {}", DICTIONARY_PATH);
        DictionarySnapshot snapshot = client.get(DICTIONARY_PATH, SNAPSHOT_TYPE);
        if (snapshot == null) {
            return new DictionarySnapshot(
                    List.of(), List.of(), List.of(), List.of(),
                    List.of(), List.of(), List.of(), List.of(), List.of());
        }
        return snapshot;
    }

    private static void appendSkills(List<SkillDTO> target, List<DictionaryEntry> source, SkillType type) {
        for (DictionaryEntry entry : source) {
            target.add(new SkillDTO(entry.id(), entry.label(), type));
        }
    }

    private static <T> List<T> map(List<DictionaryEntry> source, Function<DictionaryEntry, T> mapper) {
        return source.stream().map(mapper).toList();
    }
}