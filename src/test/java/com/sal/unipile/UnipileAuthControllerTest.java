package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccountResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        HostedAuthResponse response = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/hosted-auth-wizard-mock-123")
                .build();

        when(unipileApiClient.getHostedAuthLink(null)).thenReturn(response);

        mockMvc.perform(post("/api/v1/unipile/accounts/link")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("HostedAuthURL"))
                .andExpect(jsonPath("$.url").value("https://account.unipile.com/hosted-auth-wizard-mock-123"));
    }

    @Test
    @DisplayName("Deve criar um link de conta com sucesso")
    void createAccount_ShouldReturnUrl() throws Exception {
        HostedAuthResponse response = HostedAuthResponse.builder()
                .object("HostedAuthURL")
                .url("https://account.unipile.com/hosted-auth-wizard-mock-456")
                .build();

        when(unipileApiClient.getHostedAuthLink(null)).thenReturn(response);

        mockMvc.perform(post("/api/v1/unipile/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("HostedAuthURL"))
                .andExpect(jsonPath("$.url").value("https://account.unipile.com/hosted-auth-wizard-mock-456"));
    }

    @Test
    @DisplayName("Deve processar o callback de Hosted Auth")
    void handleHostedAuthCallback_ShouldReturnOk() throws Exception {
        HostedAuthNotification notification = HostedAuthNotification.builder()
                .status("CREATION_SUCCESS")
                .account_id("acc_123")
                .name("user123")
                .build();

        mockMvc.perform(post("/api/v1/unipile/accounts/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve listar contas com sucesso")
    void listAccounts_ShouldReturnAccounts() throws Exception {
        UnipileAccountResponse response = new UnipileAccountResponse();
        response.setObject("AccountList");

        when(unipileApiClient.listAccounts()).thenReturn(response);

        mockMvc.perform(get("/api/v1/unipile/accounts")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("AccountList"));
    }

    @Test
    @DisplayName("Deve excluir uma conta com sucesso")
    void deleteAccount_ShouldReturnOk() throws Exception {
        String accountId = "acc_123";

        mockMvc.perform(delete("/api/v1/unipile/accounts/" + accountId))
                .andExpect(status().isOk());

        verify(unipileApiClient).deleteAccount(accountId);
    }

    @Test
    @DisplayName("Deve retornar o status code do UnipileApiClient quando houver ResponseStatusException")
    void getHostedAuthLink_ShouldReturnUnipileError() throws Exception {
        org.springframework.http.HttpStatus status = org.springframework.http.HttpStatus.UNAUTHORIZED;
        String reason = "Invalid API Key";
        
        when(unipileApiClient.getHostedAuthLink(null))
                .thenThrow(new org.springframework.web.server.ResponseStatusException(status, reason));

        mockMvc.perform(post("/api/v1/unipile/accounts/link")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
