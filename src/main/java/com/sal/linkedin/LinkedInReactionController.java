package com.sal.linkedin;

import com.sal.linkedin.dto.LinkedInReactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/linkedin/likes")
public class LinkedInReactionController {

    private final LinkedInApiClient apiClient;

    @Value("${linkedin.access-token:}")
    private String defaultAccessToken;

    @Value("${linkedin.actor-urn:}")
    private String defaultActorUrn;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> react(
            @RequestHeader(name = "X-LinkedIn-Access-Token", required = false) String accessTokenHeader,
            @RequestBody Map<String, Object> body
    ) {
        try {
            // Token
            String token = StringUtils.hasText(accessTokenHeader) ? accessTokenHeader : defaultAccessToken;
            if (!StringUtils.hasText(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing LinkedIn access token");
            }

            // Campos
            String object = asText(body.get("object"));
            if (!StringUtils.hasText(object)) {
                return ResponseEntity.badRequest().body("Field 'object' (URN do post) é obrigatório");
            }

            String actor = asText(body.get("actor"));
            if (!StringUtils.hasText(actor)) {
                actor = defaultActorUrn;
            }
            if (!StringUtils.hasText(actor)) {
                return ResponseEntity.badRequest().body("Field 'actor' (URN da pessoa) é obrigatório quando 'linkedin.actor-urn' não está configurado");
            }

            String reactionType = asTextOrDefault(body.get("reactionType"), "LIKE");

            LinkedInReactionRequest request = LinkedInReactionRequest.builder()
                    .actor(actor)
                    .object(object)
                    .reactionType(reactionType)
                    .build();

            apiClient.createReaction(token, request);

            Map<String, Object> resp = new HashMap<>();
            resp.put("status", "accepted");
            resp.put("reactionType", reactionType);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resp);
        } catch (LinkedInApiException e) {
            log.warn("Erro na API do LinkedIn: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erro ao chamar LinkedIn: " + e.getMessage());
        } catch (Exception e) {
            log.error("Erro ao enviar like: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno");
        }
    }

    private String asText(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }

    private String asTextOrDefault(Object v, String def) {
        String s = asText(v);
        return StringUtils.hasText(s) ? s : def;
    }
}
