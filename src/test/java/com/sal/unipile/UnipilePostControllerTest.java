package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipilePostController.class)
class UnipilePostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve enviar uma reação com sucesso")
    void react_ShouldReturnOk() throws Exception {
        String postId = "urn:li:activity:12345";
        String accountId = "test-account";
        UnipilePostReactionRequest request = new UnipilePostReactionRequest("LIKE");

        doNothing().when(unipileApiClient).addPostReaction(eq(accountId), eq(postId), eq("LIKE"));

        mockMvc.perform(post("/api/v1/unipile/posts/{postId}/react", postId)
                        .queryParam("account_id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.reaction").value("LIKE"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando account_id não for informado e não houver default")
    void react_ShouldReturnBadRequest_WhenNoAccountId() throws Exception {
        String postId = "urn:li:activity:12345";
        UnipilePostReactionRequest request = new UnipilePostReactionRequest("LIKE");

        mockMvc.perform(post("/api/v1/unipile/posts/{postId}/react", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve buscar leads com sucesso")
    void searchLeads_ShouldReturnOk() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();
        request.setTitle("Software Engineer");
        request.setLocation("Brazil");
        request.setCompany("Google");

        UnipileLinkedInSearchResponse response = new UnipileLinkedInSearchResponse();
        response.setItems(new ArrayList<>());

        when(unipileApiClient.searchLinkedIn(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/unipile/posts/search-leads")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar todos os itens agregados de múltiplas páginas na busca de leads")
    void searchLeads_ShouldReturnAllItemsAgregated() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();
        request.setTitle("Software Engineer");

        UnipileLinkedInSearchResponse response = new UnipileLinkedInSearchResponse();
        List<UnipileLinkedInSearchResult> items = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            UnipileLinkedInSearchResult item = new UnipileLinkedInSearchResult();
            item.setId("id-" + i);
            item.setName("Person " + i);
            items.add(item);
        }
        response.setItems(items);

        Map<String, Object> paging = new HashMap<>();
        paging.put("total_count", 15);
        paging.put("page_count", 10);
        paging.put("start", 0);
        response.setPaging(paging);

        when(unipileApiClient.searchLinkedIn(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/unipile/posts/search-leads")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(15)))
                .andExpect(jsonPath("$.items[0].name").value("Person 0"))
                .andExpect(jsonPath("$.items[14].name").value("Person 14"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando account_id não for informado na busca de leads")
    void searchLeads_ShouldReturnBadRequest_WhenNoAccountId() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();

        mockMvc.perform(post("/api/v1/unipile/posts/search-leads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve enviar pedido de conexão com sucesso")
    void connect_ShouldReturnOk() throws Exception {
        String accountId = "test-account";
        UnipileConnectionRequest request = new UnipileConnectionRequest("https://linkedin.com/in/user", "Olá!");

        doNothing().when(unipileApiClient).sendConnectionRequest(eq(accountId), eq(request.getIdentifier()), eq(request.getMessage()));

        mockMvc.perform(post("/api/v1/unipile/posts/connect")
                        .queryParam("account_id", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.identifier").value(request.getIdentifier()));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando identifier não for informado no pedido de conexão")
    void connect_ShouldReturnBadRequest_WhenNoIdentifier() throws Exception {
        UnipileConnectionRequest request = new UnipileConnectionRequest("", "Olá!");

        mockMvc.perform(post("/api/v1/unipile/posts/connect")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Identifier is required"));
    }
}
