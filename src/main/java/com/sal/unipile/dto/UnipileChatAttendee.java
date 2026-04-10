package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Schema(description = "Representa um participante de um chat no Unipile")
public class UnipileChatAttendee {

    @Schema(description = "Tipo de objeto", example = "chat_attendee")
    private String object;

    @Schema(description = "ID único do participante", example = "ATT_12345")
    private String id;

    @Schema(description = "ID da conta associada", example = "ACC_67890")
    private String account_id;

    @Schema(description = "ID do provedor (LinkedIn, etc.)", example = "PRV_54321")
    private String provider_id;

    @Schema(description = "Nome do participante", example = "John Doe")
    private String name;

    @Schema(description = "Indica se o participante é o próprio usuário (1 para sim, 0 para não)", example = "0")
    private Integer is_self;

    @Schema(description = "Indica se o participante está oculto (1 para sim, 0 para não)", example = "0")
    private Integer hidden;

    @Schema(description = "URL da foto de perfil")
    private String picture_url;

    @Schema(description = "URL do perfil no provedor")
    private String profile_url;

    @Schema(description = "Detalhes específicos do provedor")
    private Object specifics;
}
