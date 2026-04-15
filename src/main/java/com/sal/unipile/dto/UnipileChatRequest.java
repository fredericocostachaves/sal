package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para iniciar um novo chat")
public class UnipileChatRequest {
    @Schema(description = "ID da conta", example = "ACC_12345")
    private String account_id;

    @Schema(description = "Lista de IDs dos participantes", example = "[\"ATT_12345\"]")
    private List<String> attendees_ids;

    @Schema(description = "Texto da mensagem inicial", example = "Olá, gostaria de conectar!")
    private String text;
}
