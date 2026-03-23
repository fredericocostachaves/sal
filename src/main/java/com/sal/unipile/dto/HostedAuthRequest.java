package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private String type; // 'create' | 'reconnect'
    private Object providers; // string[] | '*'
    private String api_url;
    private String expiresOn;
    private String notify_url;
    private String name; // internal user ID
    private String success_redirect_url;
    private String failure_redirect_url;
    private String reconnect_account;
}
