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
public class UnipileReconnectAccountRequest {
    private String account_id;
    private String type;
    private Object providers;
    private String api_url;
    private String expires_on;
    private String notify_url;
    private String name;
    private String success_redirect_url;
    private String failure_redirect_url;
}
