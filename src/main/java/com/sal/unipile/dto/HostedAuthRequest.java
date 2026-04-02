package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HostedAuthRequest {
    @JsonProperty("type")
    private String type; // 'create' | 'reconnect'
    @JsonProperty("providers")
    private Object providers; // string[] | '*'
    @JsonProperty("api_url")
    private String api_url;
    @JsonProperty("expires_on")
    private String expires_on;
    @JsonProperty("expiresOn")
    private String expiresOn;
    @JsonProperty("name")
    private String name;
    @JsonProperty("notify_url")
    private String notify_url;
    @JsonProperty("success_redirect_url")
    private String success_redirect_url;
    @JsonProperty("failure_redirect_url")
    private String failure_redirect_url;
}
