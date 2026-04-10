package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Representa uma conta conectada ao Unipile")
public class UnipileAccount {
    @Schema(description = "ID único da conta", example = "ACC_12345")
    private String id;

    @Schema(description = "Tipo de objeto", example = "account")
    private String object;

    @Schema(description = "Nome da conta", example = "John Doe (LinkedIn)")
    private String name;

    @Schema(description = "Tipo do provedor", example = "LINKEDIN")
    private String type;

    @Schema(description = "Data de criação da conta", example = "2023-10-27T10:00:00Z")
    private String created_at;

    @Schema(description = "Data da última sincronização", example = "2023-10-27T11:00:00Z")
    private String last_fetched_at;

    @Schema(description = "Assinatura atual")
    private String current_signature;

    @Schema(description = "Lista de assinaturas configuradas")
    private List<Signature> signatures;

    @Schema(description = "Lista de grupos")
    private List<Object> groups;

    @Schema(description = "Lista de fontes de dados")
    private List<Source> sources;

    @Schema(description = "Parâmetros de conexão")
    private Map<String, Object> connection_params;

    @Schema(description = "Token de sincronização")
    private String sync_token;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa uma assinatura de e-mail")
    public static class Signature {
        @Schema(description = "Título da assinatura")
        private String title;
        @Schema(description = "Conteúdo HTML da assinatura")
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa uma fonte de dados da conta")
    public static class Source {
        @Schema(description = "ID da fonte")
        private String id;
        @Schema(description = "Status da fonte", example = "OK")
        private String status;
    }
}
