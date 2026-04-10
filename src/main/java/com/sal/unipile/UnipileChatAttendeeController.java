package com.sal.unipile;

import com.sal.unipile.dto.UnipileChatAttendee;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
