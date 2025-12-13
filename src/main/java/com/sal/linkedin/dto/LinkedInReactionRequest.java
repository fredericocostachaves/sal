package com.sal.linkedin.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload para criar uma reação (like, celebrate, etc) via LinkedIn Reactions API.
 * Campos devem ser URNs válidas, ex.:
 * actor = "urn:li:person:XXXXXXXX"
 * object = "urn:li:ugcPost:YYYYYYYY" ou "urn:li:activity:ZZZZZZZZ"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LinkedInReactionRequest {
    private String actor;        // URN do membro que está reagindo
    private String object;       // URN do post/atividade/comentário
    @Builder.Default
    private String reactionType = "LIKE"; // LIKE, CELEBRATE, SUPPORT, LOVE, INSIGHTFUL, CURIOUS
}
