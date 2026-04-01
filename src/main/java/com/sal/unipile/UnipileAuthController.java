package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthRequest;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccountResponse;
import com.sal.unipile.dto.UnipileCreateAccountRequest;
import com.sal.unipile.dto.UnipileCreateAccountResponse;
import com.sal.unipile.dto.UnipileReconnectAccountRequest;
import com.sal.unipile.dto.UnipileReconnectAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hosted/accounts")
@RequiredArgsConstructor
@Slf4j
public class UnipileAuthController {

    private final UnipileApiClient unipileApiClient;

    @PostMapping("/link")
    public HostedAuthResponse getHostedAuthLink(@RequestBody HostedAuthRequest request) {
        return unipileApiClient.getHostedAuthLink(request);
    }

    @PostMapping("/callback")
    public void handleHostedAuthCallback(@RequestBody HostedAuthNotification notification) {
        log.info("Received Unipile Hosted Auth Callback: {}", notification);
        // Here you would typically match 'notification.getName()' with your internal user ID
        // and store 'notification.getAccount_id()' for future requests.
    }

    @GetMapping
    public UnipileAccountResponse listAccounts() {
        return unipileApiClient.listAccounts();
    }

    @PostMapping
    public UnipileCreateAccountResponse createAccount(@RequestBody UnipileCreateAccountRequest request) {
        log.info("Creating Unipile account for email: {}", request.getEmail());
        return unipileApiClient.createAccount(request);
    }

    @PostMapping("/reconnect")
    public UnipileReconnectAccountResponse reconnectAccount(@RequestBody UnipileReconnectAccountRequest request) {
        log.info("Generating reconnect link for account: {}", request.getAccountId());
        return unipileApiClient.reconnectAccount(request);
    }
}
