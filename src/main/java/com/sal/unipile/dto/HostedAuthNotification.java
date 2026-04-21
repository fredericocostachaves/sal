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
@Schema(description = "Notificação de status da autenticação hospedada (Webhook)")
public class HostedAuthNotification {
    @Schema(description = "Status da operação", example = "CREATION_SUCCESS")
    private String status;

    @Schema(description = "ID da conta criada ou reconectada", example = "ACC_12345")
    private String account_id;

    @Schema(description = "Nome/identificador fornecido na requisição original", example = "John Doe")
    private String name;

    @Schema(description = "Email da conta", example = "john@example.com")
    private String email;

    @Schema(description = "ID do usuário no sistema", example = "uuid-do-usuario")
    private String user_id;
}
