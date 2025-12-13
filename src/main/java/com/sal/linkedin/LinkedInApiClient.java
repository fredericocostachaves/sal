package com.sal.linkedin;

import com.sal.linkedin.dto.LinkedInReactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LinkedInApiClient {

    @Value("${linkedin.api.base-url:https://api.linkedin.com}")
    private String baseUrl;

    @Value("${linkedin.api.version:202405}")
    private String apiVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    public void createReaction(String accessToken, LinkedInReactionRequest body) {
        String url = baseUrl + "/rest/reactions?action=create";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));
        headers.add("LinkedIn-Version", apiVersion);
        headers.add("X-Restli-Protocol-Version", "2.0.0");

        HttpEntity<LinkedInReactionRequest> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            HttpStatusCode status = response.getStatusCode();
            if (!status.is2xxSuccessful()) {
                throw new LinkedInApiException("LinkedIn returned status: " + status.value() + " body=" + response.getBody());
            }
        } catch (HttpStatusCodeException ex) {
            String responseBody = ex.getResponseBodyAsString();
            log.warn("LinkedIn API error: status={} body={}", ex.getStatusCode(), responseBody);
            throw new LinkedInApiException("Error from LinkedIn API: " + ex.getStatusCode().value() + " body=" + responseBody, ex);
        } catch (Exception e) {
            throw new LinkedInApiException("Error calling LinkedIn API: " + e.getMessage(), e);
        }
    }
}
