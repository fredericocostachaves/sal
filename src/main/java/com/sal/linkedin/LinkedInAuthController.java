package com.sal.linkedin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/linkedin/auth")
@Tag(name = "LinkedIn Auth", description = "Endpoints para autenticação com LinkedIn")
public class LinkedInAuthController {

    @Value("${linkedin.client-id}")
    private String clientId;

    @Value("${linkedin.redirect-uri}")
    private String redirectUri;

    @Value("${linkedin.scope:profile}")
    private String scope;

    @Operation(summary = "Inicia fluxo de autenticação", description = "Redireciona o usuário para a página de autorização do LinkedIn.")
    @GetMapping
    public ResponseEntity<Void> authenticate() {
        String state = UUID.randomUUID().toString().replace("-", "");
        
        String authorizationUrl = UriComponentsBuilder
                .fromUriString("https://www.linkedin.com/oauth/v2/authorization")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", scope)
                .build()
                .toUriString();

        log.info("Redirecting to LinkedIn authorization: {}", authorizationUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizationUrl))
                .build();
    }
}
