package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccount;
import com.sal.unipile.dto.UnipileAccountResponse;
import com.sal.unipile.dto.UnipileReconnectAccountRequest;
import com.sal.unipile.dto.UnipileReconnectAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @PostMapping("/link")
    public HostedAuthResponse getHostedAuthLink() {
        return unipileApiClient.getHostedAuthLink(null);
    }

    @PostMapping
    public HostedAuthResponse createAccount() {
        return unipileApiClient.getHostedAuthLink(null);
    }

    @PostMapping("/callback")
    public void handleHostedAuthCallback(@RequestBody HostedAuthNotification notification) {
        log.info("Received Unipile Hosted Auth Callback: {}", notification);
        // Here you would typically match 'notification.getName()' with your internal user ID
        // and store 'notification.getAccount_id()' for future requests.
        if ("CREATION_SUCCESS".equals(notification.getStatus())) {
            log.info("Successfully linked Unipile account {} for user {}", notification.getAccount_id(), notification.getName());
        } else if ("CREATION_FAILURE".equals(notification.getStatus())) {
            log.warn("Failed to link Unipile account for user {}: {}", notification.getName(), notification.getStatus());
        }
    }

    @GetMapping
    public UnipileAccountResponse listAccounts() {
        return unipileApiClient.listAccounts();
    }

    @Operation(summary = "Obtém uma conta pelo ID", description = "Retorna os detalhes de uma conta específica pelo ID")
    @GetMapping("/{accountId}")
    public UnipileAccount getAccountById(
            @Parameter(description = "ID da conta")
            @PathVariable String accountId) {
        log.info("Getting Unipile account: {}", accountId);
        return unipileApiClient.getAccountById(accountId);
    }

    @PostMapping("/reconnect")
    public UnipileReconnectAccountResponse reconnectAccount(@RequestBody UnipileReconnectAccountRequest request) {
        log.info("Generating reconnect link for account: {}", request.getAccount_id());
        return unipileApiClient.reconnectAccount(request);
    }

    @DeleteMapping("/{accountId}")
    public void deleteAccount(@PathVariable String accountId) {
        log.info("Deleting Unipile account: {}", accountId);
        unipileApiClient.deleteAccount(accountId);
    }
}
