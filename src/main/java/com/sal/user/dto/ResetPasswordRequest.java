package com.sal.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requisição de redefinição de senha")
public class ResetPasswordRequest {

    @NotBlank
    @Schema(description = "Token de recuperação", example = "abc123-def456-ghi789")
    private String token;

    @NotBlank
    @Schema(description = "Nova senha", example = "novaSenha123")
    private String newPassword;
}