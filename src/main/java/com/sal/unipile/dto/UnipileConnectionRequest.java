package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para enviar um pedido de conexão no LinkedIn")
public class UnipileConnectionRequest {
    @Schema(description = "URL do perfil ou ID do membro no LinkedIn", example = "https://www.linkedin.com/in/william-gates-9381b/")
    private String identifier;

    @Schema(description = "Mensagem personalizada para o pedido de conexão", example = "Olá, gostaria de conectar.")
    private String message;
}
