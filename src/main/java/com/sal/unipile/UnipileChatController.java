package com.sal.unipile;

import com.sal.unipile.dto.UnipileChat;
import com.sal.unipile.dto.UnipileChatListResponse;
import com.sal.unipile.dto.UnipileChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/unipile/chats")
@Tag(name = "Unipile Chats", description = "Endpoints para interação com chats via Unipile")
public class UnipileChatController {

    private final UnipileApiClient unipileApiClient;

    @Value("${unipile.account-id:}")
    private String defaultAccountId;

    @Operation(summary = "Lista todos os chats", description = "Retorna uma lista de chats com filtros opcionais")
    @GetMapping
    public ResponseEntity<?> listAllChats(
            @Parameter(description = "Filtrar apenas chats não lidos ou lidos")
            @RequestParam(name = "unread", required = false) Boolean unread,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Filtrar itens criados antes desta data (ISO 8601 UTC)")
            @RequestParam(required = false) String before,
            @Parameter(description = "Filtrar itens criados após esta data (ISO 8601 UTC)")
            @RequestParam(required = false) String after,
            @Parameter(description = "Limite de itens retornados (1-250)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Filtrar por tipo de provedor")
            @RequestParam(name = "account_type", required = false) String accountType,
            @Parameter(description = "Filtrar por conta(s) - separado por vírgula")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChatListResponse response = unipileApiClient.listAllChats(
                    accountId, limit, cursor, unread, before, after, accountType, null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing chats via Unipile: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "Obtém detalhes de um chat", description = "Retorna os detalhes de um chat específico pelo ID")
    @GetMapping("/{chatId}")
    public ResponseEntity<?> getChat(
            @Parameter(description = "ID do chat (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChat response = unipileApiClient.getChat(chatId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting chat via Unipile: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @Operation(summary = "Inicia um novo chat", description = "Envia uma mensagem direta para um ou mais usuários via Unipile")
    @PostMapping("/start")
    public ResponseEntity<?> startChat(
            @Parameter(description = "id da conta linkedln do unipile", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileChatRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().body("Account ID is required");
        }

        if (request.getAttendees_ids() == null || request.getAttendees_ids().isEmpty()) {
            return ResponseEntity.badRequest().body("Attendees IDs are required");
        }

        if (!StringUtils.hasText(request.getText())) {
            return ResponseEntity.badRequest().body("Text is required");
        }

        try {
            unipileApiClient.startNewChat(targetAccountId, request.getAttendees_ids(), request.getText());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("attendees_ids", request.getAttendees_ids());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting chat via Unipile: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
