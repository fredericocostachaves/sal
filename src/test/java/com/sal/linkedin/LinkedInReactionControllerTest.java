package com.sal.linkedin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.linkedin.dto.LinkedInReactionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LinkedInReactionController.class)
@TestPropertySource(properties = {
        "linkedin.actor-urn=urn:li:person:TEST_ACTOR"
})
class LinkedInReactionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    LinkedInApiClient apiClient;

    @Test
    @DisplayName("Deve aceitar like com reactionType default quando campos válidos")
    void shouldAcceptLikeWithDefaults() throws Exception {
        doNothing().when(apiClient).createReaction(eq("token-123"), any(LinkedInReactionRequest.class));

        String body = "{" +
                "\"object\":\"urn:li:ugcPost:123\"" +
                "}";

        mockMvc.perform(post("/api/linkedin/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-LinkedIn-Access-Token", "token-123")
                        .content(body))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.reactionType").value("LIKE"));

        ArgumentCaptor<LinkedInReactionRequest> captor = ArgumentCaptor.forClass(LinkedInReactionRequest.class);
        verify(apiClient).createReaction(eq("token-123"), captor.capture());
        LinkedInReactionRequest sent = captor.getValue();
        assertThat(sent.getActor()).isEqualTo("urn:li:person:TEST_ACTOR");
        assertThat(sent.getObject()).isEqualTo("urn:li:ugcPost:123");
        assertThat(sent.getReactionType()).isEqualTo("LIKE");
    }

    @Test
    @DisplayName("Deve retornar 400 quando 'object' não informado")
    void shouldReturn400WhenObjectMissing() throws Exception {
        String body = "{" +
                "\"reactionType\":\"LIKE\"" +
                "}";

        mockMvc.perform(post("/api/linkedin/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-LinkedIn-Access-Token", "token-123")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 401 quando token não for enviado e não há default")
    void shouldReturn401WhenNoToken() throws Exception {
        String body = "{" +
                "\"object\":\"urn:li:ugcPost:123\"" +
                "}";

        mockMvc.perform(post("/api/linkedin/likes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
