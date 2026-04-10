package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para gerar um link de autenticação hospedada")
public class HostedAuthRequest {
    @JsonProperty("type")
    @Schema(description = "Tipo de ação (create ou reconnect)", example = "create")
    private String type; // 'create' | 'reconnect'

    @JsonProperty("providers")
    @Schema(description = "Provedores permitidos (lista de strings ou '*')", example = "[\"LINKEDIN\"]")
    private Object providers; // string[] | '*'

    @JsonProperty("api_url")
    @Schema(description = "URL da API")
    private String api_url;

    @JsonProperty("expires_on")
    @Schema(description = "Data de expiração do link")
    private String expires_on;

    @JsonProperty("expiresOn")
    @Schema(description = "Data de expiração do link (alias)")
    private String expiresOn;

    @JsonProperty("name")
    @Schema(description = "Nome para identificar a conta ou usuário", example = "John Doe")
    private String name;

    @JsonProperty("notify_url")
    @Schema(description = "URL para receber notificações de webhook")
    private String notify_url;

    @JsonProperty("success_redirect_url")
    @Schema(description = "URL para redirecionamento em caso de sucesso")
    private String success_redirect_url;

    @JsonProperty("failure_redirect_url")
    @Schema(description = "URL para redirecionamento em caso de falha")
    private String failure_redirect_url;
}
