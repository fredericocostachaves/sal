package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Requisição para busca de perfis no LinkedIn")
public class UnipileLinkedInSearchRequest {
    @JsonIgnore
    @Schema(hidden = true)
    private String account_id;
    
    @Schema(example = "Desenvolvedor Java Sênior")
    private String title;
    
    @Schema(example = "Brasil")
    private String location;
    
    @Schema(example = "Stefanini")
    private String company;
    
    @JsonIgnore
    @Schema(hidden = true)
    private String url;
}
