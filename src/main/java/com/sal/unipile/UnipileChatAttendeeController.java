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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/unipile/chat-attendees")
@RequiredArgsConstructor
@Tag(name = "Unipile Chat Attendees", description = "Endpoints para interação com participantes de chats via Unipile")
public class UnipileChatAttendeeController {

    private final UnipileApiClient unipileApiClient;

    @Operation(summary = "Obtém um participante pelo ID", description = "Retorna os detalhes de um participante de chat específico pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participante retornado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChatAttendee.class))}),
            @ApiResponse(responseCode = "404", description = "Participante não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{attendeeId}")
    public ResponseEntity<UnipileChatAttendee> getAttendeeById(
            @Parameter(description = "ID do participante")
            @PathVariable String attendeeId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChatAttendee response = unipileApiClient.getAttendeeById(attendeeId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting attendee via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Lista todos os participantes", description = "Retorna uma lista de todos os participantes de chat com filtros opcionais")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de participantes retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChatAttendeeListResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UnipileChatAttendeeListResponse> listAllAttendees(
            @Parameter(description = "Limite de itens retornados (1-250)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "Filtrar por conta")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChatAttendeeListResponse response = unipileApiClient.listAllAttendees(accountId, limit, cursor);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing attendees via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Lista chats por participante", description = "Retorna uma lista de chats associados a um participante específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de chats retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileChatListResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Participante não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{attendeeId}/chats")
    public ResponseEntity<UnipileChatListResponse> listChatsByAttendee(
            @Parameter(description = "ID do participante")
            @PathVariable String attendeeId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId,
            @Parameter(description = "Limite de itens retornados (1-250)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false) String cursor) {

        try {
            UnipileChatListResponse response = unipileApiClient.listChatsByAttendee(attendeeId, accountId, limit, cursor);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing chats by attendee via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Lista mensagens por participante", description = "Retorna uma lista de mensagens enviadas por um participante específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de mensagens retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileMessageListResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Participante não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{attendeeId}/messages")
    public ResponseEntity<UnipileMessageListResponse> listMessagesByAttendee(
            @Parameter(description = "ID do participante")
            @PathVariable String attendeeId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId,
            @Parameter(description = "Limite de itens retornados (1-250)")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Cursor para paginação")
            @RequestParam(required = false) String cursor) {

        try {
            UnipileMessageListResponse response = unipileApiClient.listMessagesByAttendee(attendeeId, accountId, limit, cursor);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error listing messages by attendee via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Obtém foto de perfil do participante", description = "Retorna a URL ou dados da foto de perfil de um participante")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto de perfil obtida com sucesso",
                    content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Participante não encontrado", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{attendeeId}/profile-picture")
    public ResponseEntity<?> getAttendeeProfilePicture(
            @Parameter(description = "ID do participante")
            @PathVariable String attendeeId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            Object response = unipileApiClient.getAttendeeProfilePicture(attendeeId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting attendee profile picture via Unipile: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
