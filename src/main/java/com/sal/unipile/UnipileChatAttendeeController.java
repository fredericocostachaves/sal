package com.sal.unipile;

import com.sal.unipile.dto.UnipileChatAttendee;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @GetMapping("/{attendeeId}")
    public ResponseEntity<?> getAttendeeById(
            @Parameter(description = "ID do participante")
            @PathVariable String attendeeId,
            @Parameter(description = "ID da conta (opcional)")
            @RequestParam(name = "account_id", required = false) String accountId) {

        try {
            UnipileChatAttendee response = unipileApiClient.getAttendeeById(attendeeId, accountId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting attendee via Unipile: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
