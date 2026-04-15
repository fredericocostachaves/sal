package com.sal.unipile.dto;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para adicionar uma reação a um post")
public class UnipilePostReactionRequest {
    @Schema(description = "Tipo de reação (LIKE, CELEBRATE, SUPPORT, LOVE, INSIGHTFUL, CURIOUS)", example = "LIKE")
    private String reaction;
}
