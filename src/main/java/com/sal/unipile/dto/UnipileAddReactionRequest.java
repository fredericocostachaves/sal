package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para adicionar reação a uma mensagem")
public class UnipileAddReactionRequest {

    @Schema(description = "Valor da reação (emoji)", required = true, example = "👍")
    private String reaction;

    @Schema(description = "ID da conta (obrigatório)", required = true, example = "ACC_12345")
    private String account_id;
}
