package com.sal.c6;

import com.sal.c6.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class C6CheckoutClient {

    @Value("${c6.checkout.base-url:https://baas-api-sandbox.c6bank.info}")
    private String baseUrl;

    @Value("${c6.checkout.client-id:}")
    private String clientId;

    @Value("${c6.checkout.client-secret:}")
    private String clientSecret;

    @Value("${c6.checkout.merchant-id:}")
    private String merchantId;

    private final RestTemplate restTemplate;

    // Simple in-memory token cache
    private volatile String accessToken;
    private volatile long tokenExpiresAtEpochSeconds = 0L;

    public C6CreatePaymentResponse createPayment(C6CreatePaymentRequest request) {
        String url = baseUrl + "/checkout/v1/payments";
        HttpHeaders headers = bearerHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (merchantId != null && !merchantId.isBlank()) {
            headers.set("X-Merchant-Id", merchantId);
        }

        HttpEntity<C6CreatePaymentRequest> entity = new HttpEntity<>(request, headers);
        log.debug("[C6] Creating payment at {}", url);
        ResponseEntity<C6CreatePaymentResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                C6CreatePaymentResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("C6 create payment error: HTTP " + response.getStatusCode().value());
        }
        return response.getBody();
    }

    public C6PaymentStatusResponse getPaymentStatus(String paymentId) {
        String url = baseUrl + "/checkout/v1/payments/" + paymentId;
        HttpHeaders headers = bearerHeaders();
        if (merchantId != null && !merchantId.isBlank()) {
            headers.set("X-Merchant-Id", merchantId);
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        log.debug("[C6] Getting payment status at {}", url);
        ResponseEntity<C6PaymentStatusResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                C6PaymentStatusResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("C6 get payment status error: HTTP " + response.getStatusCode().value());
        }
        return response.getBody();
    }

    public void cancelPayment(String paymentId) {
        String url = baseUrl + "/checkout/v1/payments/" + paymentId + "/cancel";
        HttpHeaders headers = bearerHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (merchantId != null && !merchantId.isBlank()) {
            headers.set("X-Merchant-Id", merchantId);
        }
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of(), headers);
        log.debug("[C6] Cancelling payment at {}", url);
        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("C6 cancel payment error: HTTP " + response.getStatusCode().value());
        }
    }

    public C6RefundResponse refundPayment(String paymentId, C6RefundRequest refundRequest) {
        String url = baseUrl + "/checkout/v1/payments/" + paymentId + "/refunds";
        HttpHeaders headers = bearerHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (merchantId != null && !merchantId.isBlank()) {
            headers.set("X-Merchant-Id", merchantId);
        }
        HttpEntity<C6RefundRequest> entity = new HttpEntity<>(refundRequest, headers);
        log.debug("[C6] Refunding payment at {}", url);
        ResponseEntity<C6RefundResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                C6RefundResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("C6 refund payment error: HTTP " + response.getStatusCode().value());
        }
        return response.getBody();
    }

    private HttpHeaders bearerHeaders() {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private synchronized String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (accessToken != null && now + 60 < tokenExpiresAtEpochSeconds) { // 60s safety margin
            return accessToken;
        }
        // OAuth2 Client Credentials (assumido pela documentação padrão)
        String tokenUrl = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/oauth/token")
                .queryParam("grant_type", "client_credentials")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .toUriString();

        log.debug("[C6] Fetching OAuth token at {}", tokenUrl);
        ResponseEntity<C6AuthTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()),
                C6AuthTokenResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("C6 token error: HTTP " + response.getStatusCode().value());
        }
        C6AuthTokenResponse body = response.getBody();
        this.accessToken = body.getAccess_token();
        long expiresIn = body.getExpires_in() != null ? body.getExpires_in() : 1800L;
        this.tokenExpiresAtEpochSeconds = Instant.now().getEpochSecond() + Math.max(60L, expiresIn - 30L);
        return this.accessToken;
    }
}
