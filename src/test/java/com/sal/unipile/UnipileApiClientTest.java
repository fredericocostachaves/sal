package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthRequest;
import com.sal.unipile.dto.HostedAuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class UnipileApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UnipileApiClient unipileApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(unipileApiClient, "baseUrl", "https://api.example.com");
        ReflectionTestUtils.setField(unipileApiClient, "apiKey", "test-api-key");
    }

    @Test
    void getHostedAuthLink_WithAuthDomain_ShouldRewriteUrl() {
        ReflectionTestUtils.setField(unipileApiClient, "authDomain", "auth.yourapp.com");

        HostedAuthRequest request = HostedAuthRequest.builder().type("create").build();
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/some-token")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> 
                entity.getHeaders().getFirst("X-API-KEY").equals("test-api-key")
        ), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        HostedAuthResponse result = unipileApiClient.getHostedAuthLink(request);

        assertEquals("https://auth.yourapp.com/some-token", result.getUrl());
    }

    @Test
    void getHostedAuthLink_WithoutAuthDomain_ShouldNotRewriteUrl() {
        ReflectionTestUtils.setField(unipileApiClient, "authDomain", "");

        HostedAuthRequest request = HostedAuthRequest.builder().type("create").build();
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/some-token")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> 
                entity.getHeaders().getFirst("X-API-KEY").equals("test-api-key")
        ), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        HostedAuthResponse result = unipileApiClient.getHostedAuthLink(request);

        assertEquals("https://account.unipile.com/some-token", result.getUrl());
    }
}
