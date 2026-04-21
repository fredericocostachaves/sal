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
@Schema(description = "Requisição para reconectar uma conta")
public class UnipileReconnectAccountRequest {
    @Schema(description = "ID da conta a ser reconectada", example = "ACC_12345")
    private String account_id;

    @Schema(description = "Tipo de ação", example = "reconnect")
    private String type;

    @Schema(description = "Provedores permitidos", example = "*")
    private Object providers;

    @Schema(description = "URL da API")
    private String api_url;

    @JsonProperty("expiresOn")
    @Schema(description = "Data de expiração do link")
    private String expiresOn;

    @Schema(description = "URL para notificações")
    private String notify_url;

    @Schema(description = "Nome para a conta")
    private String name;

    @Schema(description = "URL de sucesso")
    private String success_redirect_url;

    @Schema(description = "URL de falha")
    private String failure_redirect_url;
}
