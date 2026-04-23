package com.sal.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Requisição de recuperação de senha")
public class ForgotPasswordRequest {

    @NotBlank
    @Schema(description = "Email do usuário", example = "user@example.com")
    private String email;
}