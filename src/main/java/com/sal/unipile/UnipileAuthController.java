package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthRequest;
import com.sal.unipile.dto.HostedAuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/hosted/accounts")
@RequiredArgsConstructor
public class UnipileAuthController {

    private final UnipileApiClient unipileApiClient;

    @PostMapping("/link")
    public HostedAuthResponse getHostedAuthLink(@RequestBody HostedAuthRequest request) {
        return unipileApiClient.getHostedAuthLink(request);
    }
}
