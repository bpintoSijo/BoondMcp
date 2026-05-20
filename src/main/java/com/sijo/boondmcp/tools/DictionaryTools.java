package com.sijo.boondmcp.tools;

import com.sijo.boondmcp.dto.dictionary.AvailabilityStatusDTO;
import com.sijo.boondmcp.dto.dictionary.CandidateStatusesResponse;
import com.sijo.boondmcp.dto.dictionary.ContractTypeDTO;
import com.sijo.boondmcp.dto.dictionary.LanguagesResponse;
import com.sijo.boondmcp.dto.dictionary.SkillDTO;
import com.sijo.boondmcp.service.DictionaryService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DictionaryTools {

    private final DictionaryService dictionaryService;

    public DictionaryTools(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @Tool(
            name = "list_skills",
            description = "Returns all skills available in the BoondManager dictionary "
                    + "(expertise areas, tools, activity areas). Each item is tagged with its type."
    )
    public List<SkillDTO> listSkills() {
        return dictionaryService.listSkills();
    }

    @Tool(
            name = "search_skills",
            description = "Searches the BoondManager skills dictionary by a free-text query "
                    + "(case-insensitive substring match on the skill label)."
    )
    public List<SkillDTO> searchSkills(
            @ToolParam(description = "Free-text query used to filter skill labels.")
            String query
    ) {
        return dictionaryService.searchSkills(query);
    }

    @Tool(
            name = "list_languages",
            description = "Returns the BoondManager language dictionary: spoken languages and language levels."
    )
    public LanguagesResponse listLanguages() {
        return dictionaryService.listLanguages();
    }

    @Tool(
            name = "list_contract_types",
            description = "Returns the BoondManager contract types dictionary."
    )
    public List<ContractTypeDTO> listContractTypes() {
        return dictionaryService.listContractTypes();
    }

    @Tool(
            name = "list_candidate_statuses",
            description = "Returns the BoondManager candidate statuses dictionary: "
                    + "lifecycle states and evaluation values."
    )
    public CandidateStatusesResponse listCandidateStatuses() {
        return dictionaryService.listCandidateStatuses();
    }

    @Tool(
            name = "list_availability_statuses",
            description = "Returns the BoondManager candidate availability statuses dictionary."
    )
    public List<AvailabilityStatusDTO> listAvailabilityStatuses() {
        return dictionaryService.listAvailabilityStatuses();
    }
}