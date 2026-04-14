package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Requisição para enviar mensagem em um chat")
public class UnipileSendMessageRequest {

    @Schema(description = "Texto da mensagem", required = true, example = "Olá, como você está?")
    private String text;

    @Schema(description = "ID da conta (obrigatório)", required = true, example = "ACC_12345")
    private String account_id;

    @Schema(description = "ID da thread (opcional, para Slack)")
    private String thread_id;

    @Schema(description = "ID de uma mensagem para citar/responder (opcional)")
    private String quote_id;

    @Schema(description = "Duração da simulação de digitação em milissegundos (opcional, para WhatsApp)")
    private String typing_duration;
}
