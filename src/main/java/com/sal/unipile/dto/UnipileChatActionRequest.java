package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para executar ação em um chat")
public class UnipileChatActionRequest {

    @Schema(description = "Ação a ser executada (setReadStatus, setMuteStatus, setArchiveStatus, setPinnedStatus, addParticipant, removeParticipant, setLabel)", 
            required = true, example = "setReadStatus")
    private String action;

    @Schema(description = "Valor da ação (tipo depende da ação: boolean para setReadStatus/setMuteStatus/etc, string para participant IDs e labels)",
            required = true, example = "true")
    private Object value;

    @Schema(description = "ID da conta (obrigatório)", required = true, example = "ACC_12345")
    private String account_id;
}
