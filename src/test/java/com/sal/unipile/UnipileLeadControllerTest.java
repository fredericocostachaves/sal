package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.UnipileLinkedInSearchRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchResponse;
import com.sal.unipile.dto.UnipileLinkedInSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnipileLeadController.class)
@org.springframework.test.context.TestPropertySource(properties = "unipile.account-id=")
class UnipileLeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UnipileApiClient unipileApiClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchLeads_ShouldReturnOk() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();
        request.setTitle("Software Engineer");
        request.setLocation("Brazil");
        request.setCompany("Google");

        UnipileLinkedInSearchResponse response = new UnipileLinkedInSearchResponse();
        response.setItems(new ArrayList<>());

        when(unipileApiClient.searchLinkedIn(any())).thenReturn(response);

        mockMvc.perform(post("/api/unipile/leads/search")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar todos os itens agregados de múltiplas páginas")
    void searchLeads_ShouldReturnAllItemsAgregated() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();
        request.setTitle("Software Engineer");

        // Simula uma resposta que já contém os itens agregados (como o UnipileApiClient agora faz)
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

        mockMvc.perform(post("/api/unipile/leads/search")
                        .queryParam("account_id", "test-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(15)))
                .andExpect(jsonPath("$.items[0].name").value("Person 0"))
                .andExpect(jsonPath("$.items[14].name").value("Person 14"));
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando account_id não for informado")
    void searchLeads_ShouldReturnBadRequest_WhenNoAccountId() throws Exception {
        UnipileLinkedInSearchRequest request = new UnipileLinkedInSearchRequest();
        // account_id is null

        mockMvc.perform(post("/api/unipile/leads/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
