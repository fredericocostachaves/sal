package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileAccount {
    private String id;
    private String object;
    private String name;
    private String type;
    private String created_at;
    private List<Map<String, Object>> sources;
    private Map<String, Object> connection_params;
    private List<Object> groups;
}
