package com.sal.unipile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostedAuthResponse {
    private String object; // 'HostedAuthURL'
    private String url;
}
