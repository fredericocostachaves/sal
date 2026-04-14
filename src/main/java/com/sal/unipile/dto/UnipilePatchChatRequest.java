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
@Schema(description = "Requisição para atualizar um chat")
public class UnipilePatchChatRequest {

    @Schema(description = "Propriedade a ser atualizada (ex: 'archived', 'muted_until', 'pinned')", required = true, example = "archived")
    private String key;

    @Schema(description = "Novo valor da propriedade", required = true, example = "1")
    private Object value;

    @Schema(description = "ID da conta (opcional, para validação)", example = "ACC_12345")
    private String account_id;
}
