package com.sal.unipile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para sincronizar conta LinkedIn conectada")
public class LinkedInSyncRequest {

    @Schema(description = "ID da conta no Unipile", example = "e54m8LR22bA7G5qsAc8w")
    private String accountId;

    @Schema(description = "ID do usuário na tabela auth.users", example = "user-123")
    private String userId;
}