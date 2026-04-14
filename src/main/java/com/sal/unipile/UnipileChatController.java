package com.sal.unipile;

import com.sal.unipile.dto.*;
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
@RequestMapping("/api/v1/unipile/chats")
@Tag(name = "Unipile Chats", description = "Endpoints para interação com chats via Unipile")
public class UnipileChatController {

    private final UnipileApiClient unipileApiClient;

    @Value("${unipile.account-id:}")
    private String defaultAccountId;

    @Operation(summary = "Lista todos os chats", description = "Retorna uma lista de chats com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de chats retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChatListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UnipileChatListResponse> listAllChats(
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
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Obtém detalhes de um chat", description = "Retorna os detalhes de um chat específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalhes do chat retornados com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChat.class))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{chatId}")
    public ResponseEntity<UnipileChat> getChat(
            @Parameter(description = "ID do chat (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChat response = unipileApiClient.getChat(chatId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting chat via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Inicia um novo chat", description = "Envia uma mensagem direta para um ou mais usuários via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat iniciado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\", \"attendees_ids\": [\"ATT_12345\"]}"))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startChat(
            @Parameter(description = "id da conta linkedln do unipile", example = "OH0HvmubQmauwdtfr6LM3Q")
            @RequestParam(name = "account_id", required = false) String accountId,
            @RequestBody UnipileChatRequest request) {

        String targetAccountId = StringUtils.hasText(accountId) ? accountId : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        if (request.getAttendees_ids() == null || request.getAttendees_ids().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getText())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            unipileApiClient.startNewChat(targetAccountId, request.getAttendees_ids(), request.getText());

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("attendees_ids", request.getAttendees_ids());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error starting chat via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Deleta um chat", description = "Remove um chat via Unipile (suportado apenas para LinkedIn e WhatsApp)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat deletado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"object\": \"ChatDeleted\"}"))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Map<String, String>> deleteChat(
            @Parameter(description = "ID do chat a deletar (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            unipileApiClient.deleteChat(chatId, accountId);

            Map<String, String> response = new HashMap<>();
            response.put("object", "ChatDeleted");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting chat via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Atualiza um chat", description = "Modifica propriedades de um chat via Unipile (ex: marcar como lido, silenciar, fixar)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chat atualizado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChat.class))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PatchMapping("/{chatId}")
    public ResponseEntity<UnipileChat> patchChat(
            @Parameter(description = "ID do chat a atualizar (Unipile ou provider)")
            @PathVariable String chatId,
            @RequestBody UnipileChatActionRequest request) {

        try {
            unipileApiClient.patchChat(chatId, request);
            UnipileChat response = unipileApiClient.getChat(chatId, request.getAccount_id());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error patching chat via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Lista mensagens de um chat", description = "Retorna uma lista de mensagens do chat com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileMessageListResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<UnipileMessageListResponse> listChatMessages(
            @Parameter(description = "ID do chat (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId,
            @Parameter(description = "Limite de itens retornados (1-250)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Filtrar itens criados antes desta data (ISO 8601 UTC)")
            @RequestParam(required = false) String before,
            @Parameter(description = "Filtrar itens criados após esta data (ISO 8601 UTC)")
            @RequestParam(required = false) String after) {

        try {
            log.debug("Listing messages for chat: {}, account_id: {}, limit: {}", chatId, accountId, limit);
            
            UnipileMessageListResponse response = unipileApiClient.listChatMessages(chatId, accountId, limit, cursor, before, after);

            if (response != null && response.getItems() != null) {
                log.debug("Returned {} messages for chat {}", response.getItems().size(), chatId);
                
                response.getItems().forEach(msg -> {
                    if (!StringUtils.isEmpty(msg.getChat_id()) && !chatId.equals(msg.getChat_id())) {
                        log.warn("Message {} belongs to chat {} but was requested for chat {}", 
                            msg.getMessage_id(), msg.getChat_id(), chatId);
                    }
                });
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing chat messages via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Envia uma mensagem em um chat", description = "Envia uma mensagem de texto em um chat via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem enviada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\"}"))}),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessageInChat(
            @Parameter(description = "ID do chat onde enviar a mensagem")
            @PathVariable String chatId,
            @RequestBody UnipileSendMessageRequest request) {

        String targetAccountId = StringUtils.hasText(request.getAccount_id()) ? request.getAccount_id() : defaultAccountId;

        if (!StringUtils.hasText(targetAccountId)) {
            log.warn("Account ID not provided and no default configured");
            return ResponseEntity.badRequest().build();
        }

        if (!StringUtils.hasText(request.getText())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            unipileApiClient.sendMessageInChat(chatId, targetAccountId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error sending message in chat via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Lista participantes de um chat", description = "Retorna a lista de participantes de um chat via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de participantes retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChatAttendeeListResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{chatId}/attendees")
    public ResponseEntity<UnipileChatAttendeeListResponse> listChatAttendees(
            @Parameter(description = "ID do chat (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChatAttendeeListResponse response = unipileApiClient.listChatAttendees(chatId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing chat attendees via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Sincroniza histórico de chat", description = "Força a sincronização do histórico de um chat via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronização iniciada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\"}"))}),
            @ApiResponse(responseCode = "404", description = "Chat não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/{chatId}/sync-history")
    public ResponseEntity<Map<String, String>> syncChatHistory(
            @Parameter(description = "ID do chat (Unipile ou provider)")
            @PathVariable String chatId,
            @Parameter(description = "ID da conta (obrigatório se chatId for um provider ID)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            unipileApiClient.syncChatHistory(chatId, accountId);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error syncing chat history via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
