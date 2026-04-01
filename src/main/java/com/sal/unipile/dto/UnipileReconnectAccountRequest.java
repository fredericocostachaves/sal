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
    private String accountId;
    private String type;
    private Object providers;
    private String apiUrl;
    private String expiresOn;
    private String notifyUrl;
    private String name;
    private String successRedirectUrl;
    private String failureRedirectUrl;
}
