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
@Schema(description = "Requisição para atualizar uma mensagem")
public class UnipileUpdateMessageRequest {

    @Schema(description = "Novo texto da mensagem", required = true, example = "Mensagem editada")
    private String text;

    @Schema(description = "ID da conta (obrigatório)", required = true, example = "ACC_12345")
    private String account_id;
}
