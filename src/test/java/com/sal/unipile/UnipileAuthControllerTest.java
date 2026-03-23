package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthRequest;
import com.sal.unipile.dto.HostedAuthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipileAuthController.class)
class UnipileAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar o link de Hosted Auth com sucesso")
    void getHostedAuthLink_ShouldReturnUrl() throws Exception {
        HostedAuthRequest request = HostedAuthRequest.builder()
                .type("create")
                .providers("*")
                .api_url("https://api.example.com")
                .expiresOn("2026-03-20T23:59:59Z")
                .name("user123")
                .notify_url("https://yourapp.com/callback")
                .success_redirect_url("https://yourapp.com/success")
                .failure_redirect_url("https://yourapp.com/failure")
                .build();

        HostedAuthResponse response = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/hosted-auth-wizard-mock-123")
                .build();

        when(unipileApiClient.getHostedAuthLink(any(HostedAuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/hosted/accounts/link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("HostedAuthURL"))
                .andExpect(jsonPath("$.url").value("https://account.unipile.com/hosted-auth-wizard-mock-123"));
    }

    @Test
    @DisplayName("Deve processar o callback de Hosted Auth")
    void handleHostedAuthCallback_ShouldReturnOk() throws Exception {
        HostedAuthNotification notification = HostedAuthNotification.builder()
                .status("CREATION_SUCCESS")
                .account_id("acc_123")
                .name("user123")
                .build();

        mockMvc.perform(post("/api/v1/hosted/accounts/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());
    }
}
