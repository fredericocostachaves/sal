package com.sal.linkedin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/leads")
public class LinkedInLeadController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${linkedin.app-secret:}")
    private String linkedInAppSecret;

    @PostMapping(path = "/linkedin", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> captureLead(
            @RequestHeader(name = "X-LI-Signature", required = false) String signature,
            @RequestBody String rawBody,
            @RequestHeader(name = "X-LI-Event-Type", required = false) String eventType
    ) {
        try {
            if (StringUtils.hasText(linkedInAppSecret)) {
                if (!StringUtils.hasText(signature)) {
                    log.warn("LinkedIn webhook missing signature header");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing signature");
                }
                if (!isValidSignature(rawBody, signature, linkedInAppSecret)) {
                    log.warn("Invalid LinkedIn signature. EventType={}, BodyHashPrefix={}...", eventType, hashPrefix(rawBody));
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
                }
            } else {
                log.debug("linkedin.app-secret not configured; skipping signature validation (NOT recommended for production)");
            }

            Map<String, Object> payload = parsePayload(rawBody);

            // Extract a few common fields if present (best-effort)
            Object leadId = deepGet(payload, "leadId");
            Object formId = deepGet(payload, "formId");
            Object createdAt = deepGet(payload, "createdAt");
            log.info("Received LinkedIn lead: leadId={}, formId={}, createdAt={}, eventType={}", leadId, formId, createdAt, eventType);
            log.debug("Full payload: {}", payload);

            // TODO: Persist the lead to your database / queue for processing.

            return ResponseEntity.accepted().build();
        } catch (JsonProcessingException e) {
            log.warn("Invalid JSON payload from LinkedIn: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid JSON");
        } catch (Exception e) {
            log.error("Error processing LinkedIn lead: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing lead");
        }
    }

    private Map<String, Object> parsePayload(String rawBody) throws JsonProcessingException {
        return objectMapper.readValue(rawBody, Map.class);
    }

    private boolean isValidSignature(String body, String headerSignature, String secret) throws Exception {
        // Header is Base64-encoded HMAC-SHA256 of the raw request body using your app secret
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        String expected = Base64.getEncoder().encodeToString(digest);
        // trim and remove surrounding quotes if any
        String provided = sanitizeSignature(headerSignature);
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8));
    }

    private String sanitizeSignature(String sig) {
        String s = sig == null ? "" : sig.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String hashPrefix(String body) {
        // Helpful for debugging without logging full body
        int len = Math.min(body.length(), 16);
        return body.substring(0, len).replaceAll("\n", " ");
    }

    @SuppressWarnings("unchecked")
    private Object deepGet(Map<String, Object> map, String key) {
        if (map == null) return null;
        if (map.containsKey(key)) return map.get(key);
        for (Object v : map.values()) {
            if (v instanceof Map) {
                Object found = deepGet((Map<String, Object>) v, key);
                if (found != null) return found;
            }
        }
        return null;
    }
}
