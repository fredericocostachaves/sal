package com.sal.unipile.dto;

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
@Schema(description = "Requisição para envio de e-mail")
public class UnipileEmailRequest {
    @Schema(description = "Lista de destinatários principais")
    private List<EmailRecipient> to;

    @Schema(description = "Lista de destinatários em cópia")
    private List<EmailRecipient> cc;

    @Schema(description = "Lista de destinatários em cópia oculta")
    private List<EmailRecipient> bcc;

    @Schema(description = "Assunto do e-mail", example = "Reunião de Alinhamento")
    private String subject;

    @Schema(description = "Corpo do e-mail (HTML ou texto plano)", example = "Olá, segue o convite para a reunião.")
    private String body;

    @Schema(description = "Indica se deve ser salvo como rascunho", example = "false")
    private Boolean draft;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Representa um destinatário de e-mail")
    public static class EmailRecipient {
        @Schema(description = "Nome para exibição", example = "John Doe")
        private String display_name;

        @Schema(description = "Endereço de e-mail", example = "john.doe@example.com")
        private String address;
    }
}
