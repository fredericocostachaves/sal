package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
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
