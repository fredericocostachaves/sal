package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.UnipileChatRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipileChatController.class)
@TestPropertySource(properties = "unipile.account-id=")
class UnipileChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve iniciar um novo chat com sucesso")
    void startChat_ShouldReturnOk() throws Exception {
        String accountId = "test-account";
        UnipileChatRequest request = new UnipileChatRequest();
        request.setAttendees_ids(List.of("user-123"));
        request.setText("Olá, tudo bem?");

        doNothing().when(unipileApiClient).startNewChat(eq(accountId), eq(request.getAttendees_ids()), eq(request.getText()));

        mockMvc.perform(post("/api/v1/unipile/chats/start")
                        .queryParam("account_id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.attendees_ids[0]").value("user-123"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando account_id não for informado e não houver default")
    void startChat_ShouldReturnBadRequest_WhenNoAccountId() throws Exception {
        UnipileChatRequest request = new UnipileChatRequest();
        request.setAttendees_ids(List.of("user-123"));
        request.setText("Olá");

        mockMvc.perform(post("/api/v1/unipile/chats/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Account ID is required"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando attendees_ids não for informado")
    void startChat_ShouldReturnBadRequest_WhenNoAttendees() throws Exception {
        UnipileChatRequest request = new UnipileChatRequest();
        request.setText("Olá");

        mockMvc.perform(post("/api/v1/unipile/chats/start")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Attendees IDs are required"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando text não for informado")
    void startChat_ShouldReturnBadRequest_WhenNoText() throws Exception {
        UnipileChatRequest request = new UnipileChatRequest();
        request.setAttendees_ids(List.of("user-123"));

        mockMvc.perform(post("/api/v1/unipile/chats/start")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Text is required"));
    }
}
