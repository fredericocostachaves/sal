package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Resposta contendo uma lista de contas")
public class UnipileAccountResponse {
    @Schema(description = "Tipo de objeto", example = "list")
    private String object;

    @Schema(description = "Lista de contas")
    private List<UnipileAccount> items;

    @Schema(description = "Cursor para a próxima página de resultados")
    private String cursor;
}
