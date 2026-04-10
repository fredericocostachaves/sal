package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Resposta contendo o resultado da busca no LinkedIn")
public class UnipileLinkedInSearchResponse {
    @Schema(description = "Tipo de objeto", example = "list")
    private String object;

    @Schema(description = "Lista de resultados da busca")
    private List<UnipileLinkedInSearchResult> items;

    @Schema(description = "Configurações da busca")
    private Map<String, Object> config;

    @Schema(description = "Informações de paginação")
    private Map<String, Object> paging;

    @Schema(description = "Cursor para a próxima página")
    private String cursor;
}
