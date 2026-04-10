package com.sal.unipile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta contendo o link de autenticação hospedada")
public class HostedAuthResponse {
    @Schema(description = "Tipo de objeto", example = "HostedAuthURL")
    private String object; // 'HostedAuthURL'

    @Schema(description = "URL para o fluxo de autenticação hospedada", example = "https://account.unipile.com/link/...")
    private String url;
}
