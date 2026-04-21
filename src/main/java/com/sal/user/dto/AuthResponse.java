package com.sal.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação")
public class AuthResponse {

    @Schema(description = "ID do usuário")
    private String userId;

    @Schema(description = "E-mail do usuário")
    private String email;

    @Schema(description = "Token de acesso")
    private String token;
}