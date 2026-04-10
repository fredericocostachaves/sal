package com.sal.unipile;

import com.sal.unipile.dto.UnipileEmailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/unipile/emails")
@Tag(name = "Unipile Emails", description = "Endpoints para envio de e-mails via Unipile")
public class UnipileEmailController {

    private final UnipileApiClient unipileApiClient;

    @Value("${unipile.account-id:}")
    private String defaultAccountId;

    @Operation(summary = "Envia um e-mail", description = "Envia um e-mail através da API do Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "E-mail enviado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\", \"subject\": \"Assunto\", \"to\": []}"))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(
            @Parameter(description = "id da conta do unipile", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileEmailRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        if (request.getTo() == null || request.getTo().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getSubject())) {
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getBody())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            unipileApiClient.sendEmail(targetAccountId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("to", request.getTo());
            response.put("subject", request.getSubject());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending email via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
