package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileAccount {
    private String id;
    private String object;
    private String name;
    private String type;
    private String created_at;
    private String last_fetched_at;
    private String current_signature;
    private List<Signature> signatures;
    private List<Object> groups;
    private List<Source> sources;
    private Map<String, Object> connection_params;
    private String sync_token;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Signature {
        private String title;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String id;
        private String status;
    }
}
