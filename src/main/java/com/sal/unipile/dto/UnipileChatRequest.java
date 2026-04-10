package com.sal.unipile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Requisição para iniciar um novo chat")
public class UnipileChatRequest {
    @Schema(description = "Lista de IDs dos participantes", example = "[\"ATT_12345\"]")
    private List<String> attendees_ids;

    @Schema(description = "Texto da mensagem inicial", example = "Olá, gostaria de conectar!")
    private String text;
}
