package com.sal.unipile;

import com.sal.unipile.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnipileApiClient {

    @Value("${unipile.api.base-url:}")
    private String baseUrl;

    @Value("${unipile.api.key:}")
    private String apiKey;

    @Value("${unipile.api.auth-domain:}")
    private String authDomain;

    @Value("${unipile.api.ambiente:localhost}")
    private String ambiente;

    private final RestTemplate restTemplate;

    private static final DateTimeFormatter ISO_INSTANT_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private <T> T executeRequest(String uri, HttpMethod method, HttpEntity<?> entity, Class<T> responseType, String operation) {
        try {
            log.debug("Calling Unipile {}: {}", operation, uri);
            ResponseEntity<T> response = restTemplate.exchange(uri, method, entity, responseType);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                log.warn("Unipile API returned status: {} for {}", response.getStatusCode(), operation);
                throw new ResponseStatusException(response.getStatusCode(), "Unipile API error: " + response.getStatusCode().value());
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            log.warn("Unipile API error ({}): status={} body={}", operation, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new ResponseStatusException(ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
        } catch (Exception e) {
            log.error("Exception calling Unipile API ({}): {}", operation, e.getMessage(), e);
            throw new RuntimeException("Exception calling Unipile API: " + e.getMessage(), e);
        }
    }

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

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", request.getAccount_id())
                .queryParam("limit", 50);
        
        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        String linkedinUrl = buildLinkedInUrl(request);

        Map<String, String> body = new HashMap<>();
        body.put("url", linkedinUrl);
        
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, UnipileLinkedInSearchResponse.class, "LinkedIn Search page");
    }

    public void addPostReaction(String accountId, String postId, String reaction) {
        String url = baseUrl + "/api/v1/posts/" + postId + "/reactions";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        Map<String, String> body = new HashMap<>();
        body.put("reaction", reaction);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "Add Post Reaction");
    }

    public void sendConnectionRequest(String accountId, String identifier, String message) {
        String url = baseUrl + "/api/v1/linkedin/connections";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        Map<String, String> body = new HashMap<>();
        body.put("identifier", identifier);
        if (StringUtils.hasText(message)) {
            body.put("message", message);
        }

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "LinkedIn Connection Request");
    }

    public void startNewChat(String accountId, List<String> attendeesIds, String text) {
        String url = baseUrl + "/api/v1/chats";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        UnipileChatRequest request = UnipileChatRequest.builder()
                .account_id(accountId)
                .attendees_ids(attendeesIds)
                .text(text)
                .build();

        HttpEntity<UnipileChatRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "Start New Chat");
    }

    public UnipileChatListResponse listAllChats(String accountId, Integer limit, String cursor, Boolean unread,
                                                 String before, String after, String accountType, String accountIds) {
        UnipileChatListResponse combinedResponse = null;
        List<UnipileChat> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileChatListResponse pageResponse = listChatsPage(accountId, pageLimit, currentCursor, unread, before, after, accountType, accountIds);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} chats so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileChatListResponse listChatsPage(String accountId, Integer limit, String cursor, Boolean unread,
                                                   String before, String after, String accountType, String accountIds) {
        String url = baseUrl + "/api/v1/chats";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        } else if (StringUtils.hasText(accountIds)) {
            builder.queryParam("account_id", accountIds);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        if (unread != null) {
            builder.queryParam("unread", unread);
        }

        if (StringUtils.hasText(before)) {
            builder.queryParam("before", before);
        }

        if (StringUtils.hasText(after)) {
            builder.queryParam("after", after);
        }

        if (StringUtils.hasText(accountType)) {
            builder.queryParam("account_type", accountType);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChatListResponse.class, "List All Chats");
    }

    public UnipileChat getChat(String chatId, String accountId) {
        String url = baseUrl + "/api/v1/chats/" + chatId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChat.class, "Get Chat");
    }

    public UnipileChatAttendee getAttendeeById(String attendeeId, String accountId) {
        String url = baseUrl + "/api/v1/chat_attendees/" + attendeeId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChatAttendee.class, "Get Attendee By Id");
    }

    public void sendEmail(String accountId, UnipileEmailRequest request) {
        String url = baseUrl + "/api/v1/emails";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        HttpEntity<UnipileEmailRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "Send Email");
    }

    public HostedAuthResponse getHostedAuthLink(HostedAuthRequest request) {
        String url = baseUrl + "/api/v1/hosted/accounts/link";

        if (request == null) {
            request = new HostedAuthRequest();
        }
        if (!StringUtils.hasText(request.getType())) {
            request.setType("create");
        }
        if (request.getProviders() == null) {
            request.setProviders(Collections.singletonList("LINKEDIN"));
        }
        if (!StringUtils.hasText(request.getApi_url())) {
            request.setApi_url(url);
        }
        if (!StringUtils.hasText(request.getExpires_on())) {
            request.setExpires_on(ISO_INSTANT_MS.format(Instant.now().plus(1, ChronoUnit.DAYS)));
        }
        if (!StringUtils.hasText(request.getSuccess_redirect_url())) {
            request.setSuccess_redirect_url(resolveSuccessRedirectUrl());
        }

        HttpEntity<HostedAuthRequest> entity = new HttpEntity<>(request, buildHeaders());
        
        HostedAuthResponse authResponse = executeRequest(url, HttpMethod.POST, entity, HostedAuthResponse.class, "Hosted Auth Link");
        
        if (authResponse != null && StringUtils.hasText(authDomain) && authResponse.getUrl() != null) {
            String rewrittenUrl = authResponse.getUrl().replace("account.unipile.com", authDomain);
            authResponse.setUrl(rewrittenUrl);
        } else if (authResponse != null && authResponse.getUrl() != null) {
            log.warn("authDomain is not configured. Returning original Unipile URL: {}", authResponse.getUrl());
        }
        
        return authResponse;
    }

    public UnipileAccountResponse listAccounts() {
        String url = baseUrl + "/api/v1/accounts";

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(url, HttpMethod.GET, entity, UnipileAccountResponse.class, "List Accounts");
    }

    public UnipileAccount getAccountById(String accountId) {
        String url = baseUrl + "/api/v1/accounts/" + accountId;

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(url, HttpMethod.GET, entity, UnipileAccount.class, "Get Account By Id");
    }

    public UnipileReconnectAccountResponse reconnectAccount(UnipileReconnectAccountRequest request) {
        String url = baseUrl + "/api/v1/accounts/" + request.getAccount_id() + "/reconnect";

        if (!StringUtils.hasText(request.getType())) {
            request.setType("reconnect");
        }
        if (request.getProviders() == null) {
            request.setProviders("*");
        }
        if (!StringUtils.hasText(request.getApi_url())) {
            request.setApi_url(url);
        }
        if (!StringUtils.hasText(request.getExpires_on())) {
            request.setExpires_on(ISO_INSTANT_MS.format(Instant.now().plus(1, ChronoUnit.DAYS)));
        }
        if (!StringUtils.hasText(request.getSuccess_redirect_url())) {
            request.setSuccess_redirect_url(resolveSuccessRedirectUrl());
        }

        HttpEntity<UnipileReconnectAccountRequest> entity = new HttpEntity<>(request, buildHeaders());

        UnipileReconnectAccountResponse reconnectResponse = executeRequest(url, HttpMethod.POST, entity, UnipileReconnectAccountResponse.class, "Reconnect Account");

        if (reconnectResponse != null && StringUtils.hasText(authDomain) && reconnectResponse.getUrl() != null) {
            String rewrittenUrl = reconnectResponse.getUrl().replace("account.unipile.com", authDomain);
            reconnectResponse.setUrl(rewrittenUrl);
        } else if (reconnectResponse != null && reconnectResponse.getUrl() != null) {
            log.warn("authDomain is not configured. Returning original Unipile URL: {}", reconnectResponse.getUrl());
        }

        return reconnectResponse;
    }

    public void deleteAccount(String accountId) {
        String url = baseUrl + "/api/v1/accounts/" + accountId;
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());
        executeRequest(url, HttpMethod.DELETE, entity, Map.class, "Delete Account");
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

    private String resolveSuccessRedirectUrl() {
        String currentHost = null;
        try {
            currentHost = ServletUriComponentsBuilder.fromCurrentContextPath().build().getHost();
        } catch (Exception e) {
            log.debug("No active request context found, using configured environment: {}", ambiente);
        }

        if (!StringUtils.hasText(currentHost)) {
            currentHost = ambiente;
        }

        if ("localhost".equalsIgnoreCase(currentHost)) {
            return "http://localhost:3000";
        } else {
            return "https://" + currentHost + ":80";
        }
    }

    public void deleteChat(String chatId, String accountId) {
        String url = baseUrl + "/api/v1/chats/" + chatId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.DELETE, entity, Map.class, "Delete Chat");
    }

    public void patchChat(String chatId, UnipileChatActionRequest request) {
        String url = baseUrl + "/api/v1/chats/" + chatId;

        HttpEntity<UnipileChatActionRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(url, HttpMethod.PATCH, entity, Map.class, "Patch Chat");
    }

    public UnipileMessageListResponse listChatMessages(String chatId, String accountId, Integer limit, String cursor,
                                                        String before, String after) {
        UnipileMessageListResponse combinedResponse = null;
        List<UnipileMessage> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileMessageListResponse pageResponse = listChatMessagesPage(chatId, accountId, pageLimit, currentCursor, before, after);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} messages so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileMessageListResponse listChatMessagesPage(String chatId, String accountId, Integer limit, String cursor,
                                                             String before, String after) {
        String url = baseUrl + "/api/v1/chats/" + chatId + "/messages";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        if (StringUtils.hasText(before)) {
            builder.queryParam("before", before);
        }

        if (StringUtils.hasText(after)) {
            builder.queryParam("after", after);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileMessageListResponse.class, "List Chat Messages");
    }

    public void sendMessageInChat(String chatId, String accountId, UnipileSendMessageRequest request) {
        String url = baseUrl + "/api/v1/chats/" + chatId + "/messages";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("account_id", accountId);

        // Ensure account_id is set on request for consistency
        request.setAccount_id(accountId);

        // Build multipart/form-data body as required by Unipile API
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (StringUtils.hasText(request.getText())) {
            body.add("text", request.getText());
        }
        if (StringUtils.hasText(request.getAccount_id())) {
            body.add("account_id", request.getAccount_id());
        }
        if (StringUtils.hasText(request.getThread_id())) {
            body.add("thread_id", request.getThread_id());
        }
        if (StringUtils.hasText(request.getQuote_id())) {
            body.add("quote_id", request.getQuote_id());
        }
        if (request.getTyping_duration() != null) {
            body.add("typing_duration", String.valueOf(request.getTyping_duration()));
        }

        HttpHeaders headers = buildHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "Send Message In Chat");
    }

    public UnipileChatAttendeeListResponse listChatAttendees(String chatId, String accountId) {
        String url = baseUrl + "/api/v1/chats/" + chatId + "/attendees";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChatAttendeeListResponse.class, "List Chat Attendees");
    }

    public void syncChatHistory(String chatId, String accountId) {
        String url = baseUrl + "/api/v1/chats/" + chatId + "/sync-history";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.POST, entity, Map.class, "Sync Chat History");
    }

    public UnipileMessage getMessage(String messageId, String accountId) {
        String url = baseUrl + "/api/v1/messages/" + messageId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileMessage.class, "Get Message");
    }

    public UnipileMessageListResponse listAllMessages(String accountId, Integer limit, String cursor, String before, String after) {
        UnipileMessageListResponse combinedResponse = null;
        List<UnipileMessage> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileMessageListResponse pageResponse = listAllMessagesPage(accountId, pageLimit, currentCursor, before, after);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} messages so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileMessageListResponse listAllMessagesPage(String accountId, Integer limit, String cursor, String before, String after) {
        String url = baseUrl + "/api/v1/messages";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        if (StringUtils.hasText(before)) {
            builder.queryParam("before", before);
        }

        if (StringUtils.hasText(after)) {
            builder.queryParam("after", after);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileMessageListResponse.class, "List All Messages");
    }

    public void updateMessage(String messageId, UnipileUpdateMessageRequest request) {
        String url = baseUrl + "/api/v1/messages/" + messageId;

        HttpEntity<UnipileUpdateMessageRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(url, HttpMethod.PATCH, entity, Map.class, "Update Message");
    }

    public void deleteMessage(String messageId, String accountId) {
        String url = baseUrl + "/api/v1/messages/" + messageId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam("account_id", accountId);

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        executeRequest(builder.build().toUri().toString(), HttpMethod.DELETE, entity, Map.class, "Delete Message");
    }

    public void addMessageReaction(String messageId, UnipileAddReactionRequest request) {
        String url = baseUrl + "/api/v1/messages/" + messageId + "/reaction";

        HttpEntity<UnipileAddReactionRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(url, HttpMethod.POST, entity, Map.class, "Add Message Reaction");
    }

    public Map getAttachment(String messageId, String attachmentId, String accountId) {
        String url = baseUrl + "/api/v1/messages/" + messageId + "/attachments/" + attachmentId;

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, Map.class, "Get Attachment");
    }

    public void forwardMessage(String messageId, UnipileForwardMessageRequest request) {
        String url = baseUrl + "/api/v1/messages/" + messageId + "/forward";

        HttpEntity<UnipileForwardMessageRequest> entity = new HttpEntity<>(request, buildHeaders());

        executeRequest(url, HttpMethod.POST, entity, Map.class, "Forward Message");
    }

    public UnipileChatAttendeeListResponse listAllAttendees(String accountId, Integer limit, String cursor) {
        UnipileChatAttendeeListResponse combinedResponse = null;
        List<UnipileChatAttendee> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileChatAttendeeListResponse pageResponse = listAllAttendeesPage(accountId, pageLimit, currentCursor);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} attendees so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileChatAttendeeListResponse listAllAttendeesPage(String accountId, Integer limit, String cursor) {
        String url = baseUrl + "/api/v1/chat_attendees";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChatAttendeeListResponse.class, "List All Attendees");
    }

    public UnipileChatListResponse listChatsByAttendee(String attendeeId, String accountId, Integer limit, String cursor) {
        UnipileChatListResponse combinedResponse = null;
        List<UnipileChat> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileChatListResponse pageResponse = listChatsByAttendeePage(attendeeId, accountId, pageLimit, currentCursor);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} chats so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileChatListResponse listChatsByAttendeePage(String attendeeId, String accountId, Integer limit, String cursor) {
        String url = baseUrl + "/api/v1/chat_attendees/" + attendeeId + "/chats";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileChatListResponse.class, "List Chats By Attendee");
    }

    public UnipileMessageListResponse listMessagesByAttendee(String attendeeId, String accountId, Integer limit, String cursor) {
        UnipileMessageListResponse combinedResponse = null;
        List<UnipileMessage> allItems = new ArrayList<>();
        String currentCursor = cursor;
        int fetchedCount = 0;
        int maxIterations = 100;
        int pageLimit = (limit != null && limit > 0) ? Math.min(limit, 250) : 50;

        do {
            UnipileMessageListResponse pageResponse = listMessagesByAttendeePage(attendeeId, accountId, pageLimit, currentCursor);

            if (pageResponse == null || pageResponse.getItems() == null) {
                break;
            }

            if (combinedResponse == null) {
                combinedResponse = pageResponse;
            }

            allItems.addAll(pageResponse.getItems());
            fetchedCount += pageResponse.getItems().size();
            currentCursor = pageResponse.getCursor();

            log.info("Fetched {}/{} messages so far...", fetchedCount, limit != null ? limit : "unlimited");

        } while (StringUtils.hasText(currentCursor) && fetchedCount < (limit != null ? limit : Integer.MAX_VALUE) && --maxIterations > 0);

        if (combinedResponse != null) {
            combinedResponse.setItems(allItems);
        }

        return combinedResponse;
    }

    private UnipileMessageListResponse listMessagesByAttendeePage(String attendeeId, String accountId, Integer limit, String cursor) {
        String url = baseUrl + "/api/v1/chat_attendees/" + attendeeId + "/messages";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        if (limit != null && limit > 0) {
            builder.queryParam("limit", Math.min(limit, 250));
        }

        if (StringUtils.hasText(cursor)) {
            builder.queryParam("cursor", cursor);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, UnipileMessageListResponse.class, "List Messages By Attendee");
    }

    public Map getAttendeeProfilePicture(String attendeeId, String accountId) {
        String url = baseUrl + "/api/v1/chat_attendees/" + attendeeId + "/profile-picture";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);

        if (StringUtils.hasText(accountId)) {
            builder.queryParam("account_id", accountId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders());

        return executeRequest(builder.build().toUri().toString(), HttpMethod.GET, entity, Map.class, "Get Attendee Profile Picture");
    }
}
