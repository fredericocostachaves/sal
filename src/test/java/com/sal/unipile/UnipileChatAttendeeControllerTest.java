package com.sal.unipile;

import com.sal.unipile.dto.UnipileChatAttendee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipileChatAttendeeController.class)
@TestPropertySource(properties = "unipile.account-id=")
class UnipileChatAttendeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Test
    @DisplayName("Deve obter attendee por ID com sucesso")
    void getAttendeeById_ShouldReturnOk() throws Exception {
        String attendeeId = "attendee-123";
        UnipileChatAttendee attendee = UnipileChatAttendee.builder()
                .object("ChatAttendee")
                .id(attendeeId)
                .account_id("account-456")
                .provider_id("provider-789")
                .name("John Doe")
                .is_self(0)
                .hidden(0)
                .picture_url("https://example.com/picture.jpg")
                .profile_url("https://linkedin.com/in/johndoe")
                .build();

        when(unipileApiClient.getAttendeeById(eq(attendeeId), any())).thenReturn(attendee);

        mockMvc.perform(get("/api/v1/unipile/chat-attendees/{attendeeId}", attendeeId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ChatAttendee"))
                .andExpect(jsonPath("$.id").value(attendeeId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.is_self").value(0));
    }

    @Test
    @DisplayName("Deve obter attendee por ID com account_id")
    void getAttendeeById_WithAccountId_ShouldReturnOk() throws Exception {
        String attendeeId = "attendee-123";
        String accountId = "account-456";

        UnipileChatAttendee attendee = UnipileChatAttendee.builder()
                .object("ChatAttendee")
                .id(attendeeId)
                .account_id(accountId)
                .name("Jane Doe")
                .is_self(1)
                .build();

        when(unipileApiClient.getAttendeeById(eq(attendeeId), eq(accountId))).thenReturn(attendee);

        mockMvc.perform(get("/api/v1/unipile/chat-attendees/{attendeeId}", attendeeId)
                        .queryParam("account_id", accountId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ChatAttendee"))
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.is_self").value(1));
    }

    @Test
    @DisplayName("Deve retornar erro interno quando a API falhar")
    void getAttendeeById_WhenApiFails_ShouldReturnInternalServerError() throws Exception {
        when(unipileApiClient.getAttendeeById(any(), any())).thenThrow(new RuntimeException("Unipile API error"));

        mockMvc.perform(get("/api/v1/unipile/chat-attendees/{attendeeId}", "attendee-123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }
}
