package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.HostedAuthResponse;
import com.sal.unipile.dto.UnipileAccount;
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

    @MockitoBean
    private SupabaseService supabaseService;

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
                .andExpect(status().isNoContent());

        verify(unipileApiClient).deleteAccount(accountId);
    }

    @Test
    @DisplayName("Deve retornar erro interno quando houver falha na API")
    void getHostedAuthLink_ShouldReturnUnipileError() throws Exception {
        when(unipileApiClient.getHostedAuthLink(null))
                .thenThrow(new RuntimeException("Invalid API Key"));

        mockMvc.perform(post("/api/v1/unipile/accounts/link")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve obter conta por ID com sucesso")
    void getAccountById_ShouldReturnAccount() throws Exception {
        String accountId = "acc_123";
        UnipileAccount account = UnipileAccount.builder()
                .object("Account")
                .id(accountId)
                .name("Test Account")
                .type("LINKEDIN")
                .created_at("2025-01-01T00:00:00.000Z")
                .build();

        when(unipileApiClient.getAccountById(accountId)).thenReturn(account);

        mockMvc.perform(get("/api/v1/unipile/accounts/{accountId}", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("Account"))
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.name").value("Test Account"))
                .andExpect(jsonPath("$.type").value("LINKEDIN"));
    }

    @Test
    @DisplayName("Deve retornar erro quando conta não for encontrada")
    void getAccountById_WhenNotFound_ShouldReturnError() throws Exception {
        String accountId = "non_existent";

        when(unipileApiClient.getAccountById(accountId))
                .thenThrow(new RuntimeException("Account not found"));

        mockMvc.perform(get("/api/v1/unipile/accounts/{accountId}", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

}
