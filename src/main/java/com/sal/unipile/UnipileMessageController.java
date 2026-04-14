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
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/unipile/messages")
@Tag(name = "Unipile Messages", description = "Endpoints para interação com mensagens via Unipile")
public class UnipileMessageController {

    private final UnipileApiClient unipileApiClient;

    @Operation(summary = "Lista todas as mensagens", description = "Retorna uma lista de todas as mensagens com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileMessageListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UnipileMessageListResponse> listAllMessages(
            @Parameter(description = "ID da conta (obrigatório)")
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
            UnipileMessageListResponse response = unipileApiClient.listAllMessages(accountId, limit, cursor, before, after);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing messages via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Obtém uma mensagem pelo ID", description = "Retorna os detalhes de uma mensagem específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileMessage.class))}),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{messageId}")
    public ResponseEntity<UnipileMessage> getMessage(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileMessage response = unipileApiClient.getMessage(messageId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting message via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Atualiza uma mensagem", description = "Edita o texto de uma mensagem via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem atualizada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\"}"))}),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PatchMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> updateMessage(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @RequestBody UnipileUpdateMessageRequest request) {

        try {
            unipileApiClient.updateMessage(messageId, request);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating message via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Deleta uma mensagem", description = "Remove uma mensagem via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem deletada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"object\": \"MessageDeleted\"}"))}),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Map<String, String>> deleteMessage(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @Parameter(description = "ID da conta (obrigatório)")
            @RequestParam(name = "account_id") String accountId) {

        try {
            unipileApiClient.deleteMessage(messageId, accountId);

            Map<String, String> response = new HashMap<>();
            response.put("object", "MessageDeleted");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting message via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Adiciona reação a uma mensagem", description = "Adiciona um emoji/reação a uma mensagem via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reação adicionada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\"}"))}),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/{messageId}/reaction")
    public ResponseEntity<Map<String, String>> addReaction(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @RequestBody UnipileAddReactionRequest request) {

        try {
            unipileApiClient.addMessageReaction(messageId, request);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error adding reaction via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Obtém um anexo da mensagem", description = "Retorna detalhes de um anexo específico de uma mensagem")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anexo obtido com sucesso",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Anexo ou mensagem não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{messageId}/attachments/{attachmentId}")
    public ResponseEntity<?> getAttachment(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @Parameter(description = "ID do anexo")
            @PathVariable String attachmentId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            Object response = unipileApiClient.getAttachment(messageId, attachmentId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting attachment via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Encaminha uma mensagem", description = "Encaminha uma mensagem para um chat via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mensagem encaminhada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"status\": \"success\"}"))}),
            @ApiResponse(responseCode = "404", description = "Mensagem não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/{messageId}/forward")
    public ResponseEntity<Map<String, Object>> forwardMessage(
            @Parameter(description = "ID da mensagem (Unipile ou provider)")
            @PathVariable String messageId,
            @RequestBody UnipileForwardMessageRequest request) {

        if (request == null || !StringUtils.hasText(request.getChat_id())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            unipileApiClient.forwardMessage(messageId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error forwarding message via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
