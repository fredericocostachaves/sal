package com.sal.unipile;

import com.sal.unipile.dto.HostedAuthRequest;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccount;
import com.sal.unipile.dto.UnipileAccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
        ReflectionTestUtils.setField(unipileApiClient, "ambiente", "localhost");
    }

    @Test
    void getHostedAuthLink_WithRequestHost_ShouldUseRequestHost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("meu-ambiente.com");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            HostedAuthResponse mockResponse = HostedAuthResponse.builder().url("https://account.unipile.com/ok").build();
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HostedAuthResponse.class)))
                    .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            unipileApiClient.getHostedAuthLink(null);

            verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
                HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
                assertNotNull(body);
                return "https://meu-ambiente.com:80".equals(body.getSuccess_redirect_url());
            }), eq(HostedAuthResponse.class));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void getHostedAuthLink_WithLocalHostRequest_ShouldUsePort3000() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        try {
            HostedAuthResponse mockResponse = HostedAuthResponse.builder().url("https://account.unipile.com/ok").build();
            when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HostedAuthResponse.class)))
                    .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

            unipileApiClient.getHostedAuthLink(null);

            verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
                HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
                assertNotNull(body);
                return "http://localhost:3000".equals(body.getSuccess_redirect_url());
            }), eq(HostedAuthResponse.class));
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
    }

    @Test
    void getHostedAuthLink_WithoutRequest_ShouldUseConfiguredAmbiente() {
        ReflectionTestUtils.setField(unipileApiClient, "ambiente", "prod.example.com");
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder().url("https://account.unipile.com/ok").build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(null);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            assertNotNull(body);
            return "https://prod.example.com:80".equals(body.getSuccess_redirect_url());
        }), eq(HostedAuthResponse.class));
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
                Objects.equals(entity.getHeaders().getFirst("X-API-KEY"), "test-api-key")
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
                Objects.equals(entity.getHeaders().getFirst("X-API-KEY"), "test-api-key")
        ), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        HostedAuthResponse result = unipileApiClient.getHostedAuthLink(request);

        assertEquals("https://account.unipile.com/some-token", result.getUrl());
    }

    @Test
    void listAccounts_ShouldReturnAccounts() {
        UnipileAccount account = new UnipileAccount();
        account.setId("test-id");
        account.setName("Test Account");

        UnipileAccountResponse mockResponse = new UnipileAccountResponse();
        mockResponse.setObject("AccountList");
        mockResponse.setItems(Collections.singletonList(account));

        when(restTemplate.exchange(eq("https://api.example.com/api/v1/accounts"), eq(HttpMethod.GET), any(HttpEntity.class), eq(UnipileAccountResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        UnipileAccountResponse result = unipileApiClient.listAccounts();

        assertNotNull(result);
        assertEquals("AccountList", result.getObject());
        assertEquals(1, result.getItems().size());
        assertEquals("test-id", result.getItems().getFirst().getId());
    }

    @Test
    void reconnectAccount_WithAuthDomain_ShouldRewriteUrl() {
        ReflectionTestUtils.setField(unipileApiClient, "authDomain", "auth.yourapp.com");

        com.sal.unipile.dto.UnipileReconnectAccountRequest request = com.sal.unipile.dto.UnipileReconnectAccountRequest.builder()
                .account_id("acc_123")
                .build();
        
        com.sal.unipile.dto.UnipileReconnectAccountResponse mockResponse = com.sal.unipile.dto.UnipileReconnectAccountResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/reconnect-token")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(com.sal.unipile.dto.UnipileReconnectAccountResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        com.sal.unipile.dto.UnipileReconnectAccountResponse result = unipileApiClient.reconnectAccount(request);

        assertEquals("https://auth.yourapp.com/reconnect-token", result.getUrl());
    }
    @Test
    void getHostedAuthLink_ShouldUsePassedExpires_on() {
        HostedAuthRequest request = HostedAuthRequest.builder()
                .expires_on("2026-04-03T00:00:00.000Z")
                .build();
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/mock")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            return body != null && "2026-04-03T00:00:00.000Z".equals(body.getExpires_on());
        }), eq(HostedAuthResponse.class));
    }

    @Test
    void getHostedAuthLink_ShouldUseFullUrlForApiUrl_WhenMissing() {
        HostedAuthRequest request = HostedAuthRequest.builder().build();
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/mock")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            return body != null && body.getApi_url() != null && body.getApi_url().endsWith("/api/v1/hosted/accounts/link");
        }), eq(HostedAuthResponse.class));
    }

    @Test
    void getHostedAuthLink_ShouldGenerateCorrectDateFormat() {
        HostedAuthRequest request = HostedAuthRequest.builder().build();
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/mock")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            if (body == null || body.getExpires_on() == null) return false;
            // Format: YYYY-MM-DDTHH:MM:SS.sssZ
            return body.getExpires_on().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        }), eq(HostedAuthResponse.class));
    }

    @Test
    void reconnectAccount_ShouldGenerateCorrectDateFormat() {
        com.sal.unipile.dto.UnipileReconnectAccountRequest request = com.sal.unipile.dto.UnipileReconnectAccountRequest.builder()
                .account_id("acc_123")
                .build();
        
        com.sal.unipile.dto.UnipileReconnectAccountResponse mockResponse = com.sal.unipile.dto.UnipileReconnectAccountResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/reconnect-token")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(com.sal.unipile.dto.UnipileReconnectAccountResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.reconnectAccount(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            com.sal.unipile.dto.UnipileReconnectAccountRequest body = (com.sal.unipile.dto.UnipileReconnectAccountRequest) entity.getBody();
            if (body == null || body.getExpires_on() == null) return false;
            // Format: YYYY-MM-DDTHH:MM:SS.sssZ
            return body.getExpires_on().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
        }), eq(com.sal.unipile.dto.UnipileReconnectAccountResponse.class));
    }

    @Test
    void getHostedAuthLink_ShouldSetSuccessRedirectUrlForLocalhost() {
        ReflectionTestUtils.setField(unipileApiClient, "ambiente", "localhost");
        HostedAuthRequest request = HostedAuthRequest.builder().build();
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/mock")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            return body != null && "http://localhost:3000".equals(body.getSuccess_redirect_url());
        }), eq(HostedAuthResponse.class));
    }

    @Test
    void getHostedAuthLink_ShouldSetSuccessRedirectUrlForOtherEnvironment() {
        ReflectionTestUtils.setField(unipileApiClient, "ambiente", "dev.sal.com");
        HostedAuthRequest request = HostedAuthRequest.builder().build();
        
        HostedAuthResponse mockResponse = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/mock")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(HostedAuthResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.getHostedAuthLink(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            HostedAuthRequest body = (HostedAuthRequest) entity.getBody();
            return body != null && "https://dev.sal.com:80".equals(body.getSuccess_redirect_url());
        }), eq(HostedAuthResponse.class));
    }

    @Test
    void reconnectAccount_ShouldSetSuccessRedirectUrl() {
        ReflectionTestUtils.setField(unipileApiClient, "ambiente", "prod.sal.com");
        com.sal.unipile.dto.UnipileReconnectAccountRequest request = com.sal.unipile.dto.UnipileReconnectAccountRequest.builder()
                .account_id("acc_123")
                .build();
        
        com.sal.unipile.dto.UnipileReconnectAccountResponse mockResponse = com.sal.unipile.dto.UnipileReconnectAccountResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/reconnect-token")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(com.sal.unipile.dto.UnipileReconnectAccountResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        unipileApiClient.reconnectAccount(request);

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), argThat(entity -> {
            com.sal.unipile.dto.UnipileReconnectAccountRequest body = (com.sal.unipile.dto.UnipileReconnectAccountRequest) entity.getBody();
            return body != null && "https://prod.sal.com:80".equals(body.getSuccess_redirect_url());
        }), eq(com.sal.unipile.dto.UnipileReconnectAccountResponse.class));
    }

    @Test
    void deleteAccount_ShouldCallDeleteEndpoint() {
        String accountId = "acc_123";
        String expectedUrl = "https://api.example.com/api/v1/accounts/" + accountId;

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        unipileApiClient.deleteAccount(accountId);

        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void executeRequest_OnHttpStatusCodeException_ShouldThrowResponseStatusException() {
        String accountId = "acc_123";
        String expectedUrl = "https://api.example.com/api/v1/accounts/" + accountId;
        String responseBody = "{\"error\":\"invalid_request\"}";
        
        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new org.springframework.web.client.HttpClientErrorException(
                        HttpStatus.BAD_REQUEST, "Bad Request", responseBody.getBytes(), java.nio.charset.StandardCharsets.UTF_8));

        org.springframework.web.server.ResponseStatusException ex = org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class, 
                () -> unipileApiClient.deleteAccount(accountId)
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals(responseBody, ex.getReason());
    }

    @Test
    void sendMessageInChat_ShouldUseFormData() {
        String chatId = "chat_123";
        String accountId = "acc_456";
        com.sal.unipile.dto.UnipileSendMessageRequest request = com.sal.unipile.dto.UnipileSendMessageRequest.builder()
                .text("Hello")
                .build();

        String expectedUrl = "https://api.example.com/api/v1/chats/" + chatId + "/messages?account_id=" + accountId;

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Collections.singletonMap("id", "msg_123"), HttpStatus.OK));

        unipileApiClient.sendMessageInChat(chatId, accountId, request);

        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.POST), argThat(entity -> {
            Object body = entity.getBody();
            if (!(body instanceof org.springframework.util.MultiValueMap)) return false;
            org.springframework.util.MultiValueMap mv = (org.springframework.util.MultiValueMap) body;
            if (!mv.containsKey("text")) return false;
            Object textVal = mv.getFirst("text");
            boolean textOk = "Hello".equals(textVal);
            org.springframework.http.MediaType ct = entity.getHeaders().getContentType();
            boolean contentTypeOk = ct != null && org.springframework.http.MediaType.MULTIPART_FORM_DATA.includes(ct);
            return textOk && accountId.equals(mv.getFirst("account_id")) && contentTypeOk;
        }), eq(Map.class));
    }

    @Test
    void startNewChat_ShouldUseJson() {
        String accountId = "acc_456";
        java.util.List<String> attendeesIds = java.util.List.of("att_1", "att_2");
        String text = "Start";

        String expectedUrl = "https://api.example.com/api/v1/chats?account_id=" + accountId;

        when(restTemplate.exchange(eq(expectedUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Collections.singletonMap("id", "chat_123"), HttpStatus.OK));

        unipileApiClient.startNewChat(accountId, attendeesIds, text);

        verify(restTemplate).exchange(eq(expectedUrl), eq(HttpMethod.POST), argThat(entity -> {
            Object body = entity.getBody();
            if (!(body instanceof com.sal.unipile.dto.UnipileChatRequest)) return false;
            com.sal.unipile.dto.UnipileChatRequest req = (com.sal.unipile.dto.UnipileChatRequest) body;
            return "Start".equals(req.getText()) &&
                   accountId.equals(req.getAccount_id()) &&
                   attendeesIds.equals(req.getAttendees_ids()) &&
                   org.springframework.http.MediaType.APPLICATION_JSON.includes(entity.getHeaders().getContentType());
        }), eq(Map.class));
    }
}
