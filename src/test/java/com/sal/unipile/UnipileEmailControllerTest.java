package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.UnipileEmailRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipileEmailController.class)
@org.springframework.test.context.TestPropertySource(properties = "unipile.account-id=")
class UnipileEmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve enviar um e-mail com sucesso")
    void sendEmail_ShouldReturnOk() throws Exception {
        String accountId = "test-account";
        UnipileEmailRequest request = UnipileEmailRequest.builder()
                .to(List.of(UnipileEmailRequest.EmailRecipient.builder()
                        .display_name("Recipient")
                        .address("recipient@example.com")
                        .build()))
                .subject("Test Subject")
                .body("Test Body")
                .build();

        doNothing().when(unipileApiClient).sendEmail(eq(accountId), eq(request));

        mockMvc.perform(post("/api/unipile/emails/send")
                        .queryParam("account_id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando account_id não for informado e não houver default")
    void sendEmail_ShouldReturnBadRequest_WhenNoAccountId() throws Exception {
        UnipileEmailRequest request = UnipileEmailRequest.builder()
                .to(List.of(UnipileEmailRequest.EmailRecipient.builder()
                        .display_name("Recipient")
                        .address("recipient@example.com")
                        .build()))
                .subject("Test Subject")
                .body("Test Body")
                .build();

        mockMvc.perform(post("/api/unipile/emails/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Account ID is required"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando destinatário (to) não for informado")
    void sendEmail_ShouldReturnBadRequest_WhenNoTo() throws Exception {
        UnipileEmailRequest request = UnipileEmailRequest.builder()
                .subject("Test Subject")
                .body("Test Body")
                .build();

        mockMvc.perform(post("/api/unipile/emails/send")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Recipient (to) is required"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando assunto não for informado")
    void sendEmail_ShouldReturnBadRequest_WhenNoSubject() throws Exception {
        UnipileEmailRequest request = UnipileEmailRequest.builder()
                .to(List.of(UnipileEmailRequest.EmailRecipient.builder()
                        .display_name("Recipient")
                        .address("recipient@example.com")
                        .build()))
                .body("Test Body")
                .build();

        mockMvc.perform(post("/api/unipile/emails/send")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Subject is required"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando corpo não for informado")
    void sendEmail_ShouldReturnBadRequest_WhenNoBody() throws Exception {
        UnipileEmailRequest request = UnipileEmailRequest.builder()
                .to(List.of(UnipileEmailRequest.EmailRecipient.builder()
                        .display_name("Recipient")
                        .address("recipient@example.com")
                        .build()))
                .subject("Test Subject")
                .build();

        mockMvc.perform(post("/api/unipile/emails/send")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Body is required"));
    }
}
