package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccount;
import com.sal.unipile.dto.UnipileAccountResponse;
import com.sal.unipile.dto.UnipileReconnectAccountRequest;
import com.sal.unipile.dto.UnipileReconnectAccountResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/unipile/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Unipile Accounts", description = "Endpoints para gerenciamento de contas Unipile")
public class UnipileAuthController {

    private final UnipileApiClient unipileApiClient;

    @Operation(summary = "Gera um link de autenticação hospedada", description = "Retorna um link para o usuário realizar a conexão de uma conta via Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link gerado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = HostedAuthResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/link")
    public ResponseEntity<HostedAuthResponse> getHostedAuthLink() {
        try {
            return ResponseEntity.ok(unipileApiClient.getHostedAuthLink(null));
        } catch (Exception e) {
            log.error("Error generating hosted auth link: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Cria uma nova conta", description = "Inicia o processo de criação de conta via autenticação hospedada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Processo iniciado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = HostedAuthResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping
    public ResponseEntity<HostedAuthResponse> createAccount() {
        try {
            return ResponseEntity.ok(unipileApiClient.getHostedAuthLink(null));
        } catch (Exception e) {
            log.error("Error creating account: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Callback de autenticação hospedada", description = "Recebe notificações do Unipile sobre o status da autenticação")
    @PostMapping("/callback")
    public ResponseEntity<Void> handleHostedAuthCallback(@RequestBody HostedAuthNotification notification) {
        log.info("Received Unipile Hosted Auth Callback: {}", notification);
        if ("CREATION_SUCCESS".equals(notification.getStatus())) {
            log.info("Successfully linked Unipile account {} for user {}", notification.getAccount_id(), notification.getName());
        } else if ("CREATION_FAILURE".equals(notification.getStatus())) {
            log.warn("Failed to link Unipile account for user {}: {}", notification.getName(), notification.getStatus());
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Lista todas as contas", description = "Retorna uma lista de todas as contas conectadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contas retornadas com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileAccountResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping
    public ResponseEntity<UnipileAccountResponse> listAccounts() {
        try {
            return ResponseEntity.ok(unipileApiClient.listAccounts());
        } catch (Exception e) {
            log.error("Error listing accounts: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Obtém uma conta pelo ID", description = "Retorna os detalhes de uma conta específica pelo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta retornada com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileAccount.class))}),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<UnipileAccount> getAccountById(
            @Parameter(description = "ID da conta")
            @PathVariable String accountId) {
        log.info("Getting Unipile account: {}", accountId);
        try {
            return ResponseEntity.ok(unipileApiClient.getAccountById(accountId));
        } catch (Exception e) {
            log.error("Error getting account {}: {}", accountId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Reconecta uma conta", description = "Gera um link para reconectar uma conta que perdeu o acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Link gerado com sucesso",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UnipileReconnectAccountResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @PostMapping("/reconnect")
    public ResponseEntity<UnipileReconnectAccountResponse> reconnectAccount(@RequestBody UnipileReconnectAccountRequest request) {
        log.info("Generating reconnect link for account: {}", request.getAccount_id());
        try {
            return ResponseEntity.ok(unipileApiClient.reconnectAccount(request));
        } catch (Exception e) {
            log.error("Error reconnecting account {}: {}", request.getAccount_id(), e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Deleta uma conta", description = "Remove permanentemente uma conta do Unipile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta deletada com sucesso"),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor", content = @Content)
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountId) {
        log.info("Deleting Unipile account: {}", accountId);
        try {
            unipileApiClient.deleteAccount(accountId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting account {}: {}", accountId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }
}
