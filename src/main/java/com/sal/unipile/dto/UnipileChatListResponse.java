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
@Schema(description = "Resposta contendo uma lista de chats")
public class UnipileChatListResponse {
    @Schema(description = "Tipo de objeto", example = "list")
    private String object;

    @Schema(description = "Lista de chats")
    private List<UnipileChat> items;

    @Schema(description = "Cursor para a próxima página de resultados")
    private String cursor;
}
