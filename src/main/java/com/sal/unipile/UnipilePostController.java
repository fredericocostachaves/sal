package com.sal.unipile;

import com.sal.unipile.dto.UnipileConnectionRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchResponse;
import com.sal.unipile.dto.UnipilePostReactionRequest;
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
@RequestMapping("/api/v1/unipile/posts")
@Tag(name = "Unipile Posts", description = "Endpoints para interação com posts via Unipile")
public class UnipilePostController {

    private final UnipileApiClient unipileApiClient;

    @Value("${unipile.account-id:}")
    private String defaultAccountId;

    @Operation(summary = "Adiciona uma reação a um post", description = "Envia uma reação (LIKE, etc.) para um post no LinkedIn via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reação adicionada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\", \"postId\": \"urn:li:activity:123\", \"reaction\": \"LIKE\"}"))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/{postId}/react")
    public ResponseEntity<Map<String, String>> react(
            @Parameter(description = "ID do post", example = "urn:li:activity:123456789")
            @PathVariable String postId,
            @Parameter(description = "id da conta linkedln do unipile", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipilePostReactionRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getReaction())) {
            request.setReaction("LIKE");
        }

        try {
            unipileApiClient.addPostReaction(targetAccountId, postId, request.getReaction());
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("postId", postId);
            response.put("reaction", request.getReaction());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding reaction to post via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Busca leads no LinkedIn", description = "Realiza uma busca de perfis no LinkedIn através da Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileLinkedInSearchResponse.class))}),
            @ApiResponse(responseCode = "204", description = "Nenhum resultado encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/search-leads")
    public ResponseEntity<UnipileLinkedInSearchResponse> searchLeads(
            @Parameter(description = "id da conta linkedln do unipile (conta que irá fazer as requisições)", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileLinkedInSearchRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        request.setAccount_id(targetAccountId);

        try {
            UnipileLinkedInSearchResponse response = unipileApiClient.searchLinkedIn(request);
            if (response == null) {
                log.warn("Unipile API returned empty response");
                return ResponseEntity.noContent().build();
            }
            log.info("Found {} results from Unipile search", response.getItems() != null ? response.getItems().size() : 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching leads via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Envia pedido de conexão", description = "Envia um pedido de conexão para um usuário no LinkedIn via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido enviado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\", \"identifier\": \"https://...\"}"))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/connect")
    public ResponseEntity<Map<String, String>> connect(
            @Parameter(description = "id da conta linkedln do unipile", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileConnectionRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getIdentifier())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            unipileApiClient.sendConnectionRequest(targetAccountId, request.getIdentifier(), request.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("identifier", request.getIdentifier());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending connection request via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
