package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Representa um resultado individual de busca no LinkedIn")
public class UnipileLinkedInSearchResult {
    @Schema(description = "Tipo do resultado", example = "member")
    private String type;

    @Schema(description = "Indústria", example = "Tecnologia da Informação")
    private String industry;

    @Schema(description = "ID único", example = "ACoAA...")
    private String id;

    @Schema(description = "Nome do perfil", example = "John Doe")
    private String name;

    @Schema(description = "URN do membro")
    private String member_urn;

    @Schema(description = "Identificador público")
    private String public_identifier;

    @Schema(description = "URL do perfil")
    private String profile_url;

    @Schema(description = "URL pública do perfil")
    private String public_profile_url;

    @Schema(description = "URL da foto de perfil")
    private String profile_picture_url;

    @Schema(description = "URL da foto de perfil (grande)")
    private String profile_picture_url_large;

    @Schema(description = "Distância na rede (ex: DISTANCE_1)")
    private String network_distance;

    @Schema(description = "Localização", example = "São Paulo, Brasil")
    private String location;

    @Schema(description = "Título profissional (Headline)", example = "Senior Software Engineer")
    private String headline;

    @Schema(description = "Indica se o perfil é verificado")
    private Boolean verified;

    @Schema(description = "Indica se o perfil é premium")
    private Boolean premium;
}
