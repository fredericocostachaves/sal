package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.UnipileChat;
import com.sal.unipile.dto.UnipileChatListResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve listar todos os chats com sucesso")
    void listAllChats_ShouldReturnOk() throws Exception {
        UnipileChatListResponse response = new UnipileChatListResponse();
        response.setObject("ChatList");

        UnipileChat chat = new UnipileChat();
        chat.setObject("Chat");
        chat.setId("chat-123");
        chat.setAccount_id("test-account");
        chat.setAccount_type("LINKEDIN");
        chat.setProvider_id("provider-123");
        chat.setName("John Doe");
        chat.setType(1);
        chat.setUnread_count(0);
        chat.setArchived(0);
        chat.setMuted_until(-1);
        chat.setRead_only(0);
        chat.setPinned(0);

        response.setItems(List.of(chat));

        when(unipileApiClient.listAllChats(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/unipile/chats")
                        .queryParam("account_id", "test-account")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ChatList"))
                .andExpect(jsonPath("$.items[0].object").value("Chat"))
                .andExpect(jsonPath("$.items[0].id").value("chat-123"));
    }

    @Test
    @DisplayName("Deve listar chats filtrados por unread")
    void listAllChats_WithUnreadFilter_ShouldReturnOk() throws Exception {
        UnipileChatListResponse response = new UnipileChatListResponse();
        response.setObject("ChatList");
        response.setItems(List.of());

        when(unipileApiClient.listAllChats(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/unipile/chats")
                        .queryParam("unread", "true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ChatList"));
    }

    @Test
    @DisplayName("Deve listar chats com limite e cursor")
    void listAllChats_WithLimitAndCursor_ShouldReturnOk() throws Exception {
        UnipileChatListResponse response = new UnipileChatListResponse();
        response.setObject("ChatList");
        response.setItems(List.of());
        response.setCursor("next-page-cursor");

        when(unipileApiClient.listAllChats(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/unipile/chats")
                        .queryParam("limit", "50")
                        .queryParam("cursor", "initial-cursor")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ChatList"))
                .andExpect(jsonPath("$.cursor").value("next-page-cursor"));
    }

    @Test
    @DisplayName("Deve retornar erro interno quando a API falhar")
    void listAllChats_WhenApiFails_ShouldReturnInternalServerError() throws Exception {
        when(unipileApiClient.listAllChats(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Unipile API error"));

        mockMvc.perform(get("/api/v1/unipile/chats")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Deve obter chat por ID com sucesso")
    void getChat_ShouldReturnOk() throws Exception {
        String chatId = "chat-123";
        UnipileChat chat = new UnipileChat();
        chat.setObject("Chat");
        chat.setId(chatId);
        chat.setAccount_id("test-account");
        chat.setAccount_type("LINKEDIN");
        chat.setProvider_id("provider-123");
        chat.setName("John Doe");
        chat.setType(1);
        chat.setUnread_count(0);
        chat.setArchived(0);
        chat.setMuted_until(-1);
        chat.setRead_only(0);
        chat.setPinned(0);

        when(unipileApiClient.getChat(eq(chatId), any())).thenReturn(chat);

        mockMvc.perform(get("/api/v1/unipile/chats/{chatId}", chatId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("Chat"))
                .andExpect(jsonPath("$.id").value(chatId))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("Deve obter chat por ID com account_id")
    void getChat_WithAccountId_ShouldReturnOk() throws Exception {
        String chatId = "chat-123";
        String accountId = "account-456";

        UnipileChat chat = new UnipileChat();
        chat.setObject("Chat");
        chat.setId(chatId);
        chat.setAccount_id(accountId);

        when(unipileApiClient.getChat(eq(chatId), eq(accountId))).thenReturn(chat);

        mockMvc.perform(get("/api/v1/unipile/chats/{chatId}", chatId)
                        .queryParam("account_id", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("Chat"))
                .andExpect(jsonPath("$.id").value(chatId))
                .andExpect(jsonPath("$.account_id").value(accountId));
    }

    @Test
    @DisplayName("Deve retornar erro interno quando getChat falhar")
    void getChat_WhenApiFails_ShouldReturnInternalServerError() throws Exception {
        when(unipileApiClient.getChat(any(), any())).thenThrow(new RuntimeException("Unipile API error"));

        mockMvc.perform(get("/api/v1/unipile/chats/{chatId}", "chat-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
