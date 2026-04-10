package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Resposta contendo o link para reconexão de conta")
public class UnipileReconnectAccountResponse {
    @Schema(description = "Tipo de objeto", example = "HostedAuthURL")
    private String object;

    @Schema(description = "URL para reconexão")
    private String url;
}
