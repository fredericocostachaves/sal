package com.sal.c6.dto;

import lombok.Data;

@Data
public class C6AuthTokenResponse {
    private String access_token;
    private String token_type;
    private Long expires_in;
}
