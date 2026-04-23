package com.sal.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requisição de troca de senha")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "Senha atual", example = "senha123")
    private String currentPassword;

    @NotBlank
    @Schema(description = "Nova senha", example = "novaSenha123")
    private String newPassword;
}