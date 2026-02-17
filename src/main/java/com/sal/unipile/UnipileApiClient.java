package com.sal.unipile;

import com.sal.unipile.dto.UnipileEmailRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchRequest;
import com.sal.unipile.dto.UnipileLinkedInSearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnipileApiClient {

    @Value("${unipile.api.base-url:https://api1.unipile.com:1337}")
    private String baseUrl;

    @Value("${unipile.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public UnipileLinkedInSearchResponse searchLinkedIn(UnipileLinkedInSearchRequest request) {
        UnipileLinkedInSearchResponse combinedResponse = null;
        List<com.sal.unipile.dto.UnipileLinkedInSearchResult> allItems = new ArrayList<>();
        String currentCursor = null;
        int totalToFetch = Integer.MAX_VALUE;
        int fetchedCount = 0;

        do {
            UnipileLinkedInSearchResponse pageResponse = searchLinkedInPage(request, currentCursor);
            
            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
                if (pageResponse.getPaging() != null && pageResponse.getPaging().get("total_count") != null) {
                    totalToFetch = ((Number) pageResponse.getPaging().get("total_count")).intValue();
                }
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} items so far...", fetchedCount, totalToFetch);

        } while (StringUtils.hasText(currentCursor) && fetchedCount < totalToFetch);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileLinkedInSearchResponse searchLinkedInPage(UnipileLinkedInSearchRequest request, String cursor) {
        String url = baseUrl + "/api/v1/linkedin/search";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", request.getAccount_id())
                .queryParam("limit", 50);
        
        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        String linkedinUrl = buildLinkedInUrl(request);

        Map<String, String> body = new HashMap<>();
        body.put("url", linkedinUrl);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Calling Unipile LinkedIn Search page (POST): {}", builder.build().toUri());
            ResponseEntity<UnipileLinkedInSearchResponse> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    UnipileLinkedInSearchResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.error("Unipile API returned status: {}", response.getStatusCode());
                throw new RuntimeException("Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.error("Unipile API error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error from Unipile API: " + ex.getStatusCode().value() + " body=" + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API: {}", e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

    public void addPostReaction(String accountId, String postId, String reaction) {
        String url = baseUrl + "/api/v1/posts/" + postId + "/reactions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        Map<String, String> body = new HashMap<>();
        body.put("reaction", reaction);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Calling Unipile Add Post Reaction (POST): {}", builder.build().toUri());
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Unipile API returned status: {}", response.getStatusCode());
                throw new RuntimeException("Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.error("Unipile API error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error from Unipile API: " + ex.getStatusCode().value() + " body=" + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API: {}", e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

    public void sendConnectionRequest(String accountId, String identifier, String message) {
        String url = baseUrl + "/api/v1/users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        Map<String, String> body = new HashMap<>();
        body.put("identifier", identifier);
        if (StringUtils.hasText(message)) {
            body.put("message", message);
        }

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Calling Unipile Add User (POST): {}", builder.build().toUri());
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Unipile API returned status: {}", response.getStatusCode());
                throw new RuntimeException("Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.error("Unipile API error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error from Unipile API: " + ex.getStatusCode().value() + " body=" + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API: {}", e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

    public void startNewChat(String accountId, List<String> attendeesIds, String text) {
        String url = baseUrl + "/api/v1/chats";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        Map<String, Object> body = new HashMap<>();
        body.put("attendees_ids", attendeesIds);
        body.put("text", text);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.debug("Calling Unipile Start New Chat (POST): {}", builder.build().toUri());
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Unipile API returned status: {}", response.getStatusCode());
                throw new RuntimeException("Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.error("Unipile API error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error from Unipile API: " + ex.getStatusCode().value() + " body=" + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API: {}", e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

    public void sendEmail(String accountId, UnipileEmailRequest request) {
        String url = baseUrl + "/api/v1/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        HttpEntity<UnipileEmailRequest> entity = new HttpEntity<>(request, headers);

        try {
            log.debug("Calling Unipile Send Email (POST): {}", builder.build().toUri());
            ResponseEntity<Map> response = restTemplate.exchange(
                    builder.build().toUri(),
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Unipile API returned status: {}", response.getStatusCode());
                throw new RuntimeException("Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.error("Unipile API error: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new RuntimeException("Error from Unipile API: " + ex.getStatusCode().value() + " body=" + ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API: {}", e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

    private String buildLinkedInUrl(UnipileLinkedInSearchRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://www.linkedin.com/search/results/people/");
        
        if (StringUtils.hasText(request.getTitle())) {
            builder.queryParam("keywords", request.getTitle());
        }
        if (StringUtils.hasText(request.getLocation())) {
            builder.queryParam("location", request.getLocation());
        }
        if (StringUtils.hasText(request.getCompany())) {
            builder.queryParam("company", request.getCompany());
        }
        builder.queryParam("category", "PEOPLE");
        
        return builder.toUriString();
    }
}
