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
@Schema(description = "Requisição para encaminhar uma mensagem")
public class UnipileForwardMessageRequest {

    @Schema(description = "ID do chat para onde encaminhar a mensagem", required = true, example = "CHT_12345")
    private String chat_id;

    @Schema(description = "ID da conta (obrigatório)", required = true, example = "ACC_12345")
    private String account_id;
}
