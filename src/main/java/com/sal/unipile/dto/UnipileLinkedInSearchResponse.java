package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileLinkedInSearchResponse {
    private String object;
    private List<UnipileLinkedInSearchResult> items;
    private Map<String, Object> config;
    private Map<String, Object> paging;
    private String cursor;
}
