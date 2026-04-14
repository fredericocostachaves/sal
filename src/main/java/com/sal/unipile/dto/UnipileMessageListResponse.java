package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Resposta contendo uma lista de mensagens")
public class UnipileMessageListResponse {
    @Schema(description = "Tipo de objeto", example = "list")
    private String object;

    @Schema(description = "Lista de mensagens")
    private List<UnipileMessage> items;

    @Schema(description = "Cursor para a próxima página de resultados")
    private String cursor;
}
