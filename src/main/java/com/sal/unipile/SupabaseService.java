package com.sal.unipile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sal.unipile.dto.HostedAuthNotification;
import com.sal.unipile.dto.UnipileAccount;
import com.sal.unipile.persistence.Account;
import com.sal.unipile.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseService {

    private final RestTemplateBuilder restTemplateBuilder;
    private final ObjectMapper objectMapper;
    private final AccountRepository accountRepository;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Value("${supabase.key:}")
    private String supabaseKey;

    public void saveAccount(HostedAuthNotification n, String userId) {
        if (supabaseUrl == null || supabaseUrl.isEmpty() || supabaseKey == null || supabaseKey.isEmpty()) {
            log.warn("Supabase not configured; skipping saveAccount");
            return;
        }
        try {
            RestTemplate rest = restTemplateBuilder.build();
            String url = supabaseUrl;
            if (!url.endsWith("/")) url += "/";
            url += "rest/v1/accounts";

            Map<String, Object> payload = new HashMap<>();
            payload.put("user_id", userId);
            payload.put("name", n.getName());
            payload.put("status", mapStatusFromCallback(n.getStatus()));
            payload.put("initials", extractInitials(n.getName()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("Prefer", "return=representation");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> resp = rest.postForEntity(url, entity, String.class);

            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("Saved account to Supabase via REST: {}", n.getEmail());
            } else {
                log.warn("Failed to save account to Supabase: status={} body={}", resp.getStatusCodeValue(), resp.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to save account to Supabase", e);
        }
    }

    public void syncAccounts(List<UnipileAccount> accounts, String userId) {
        if (accountRepository == null) {
            log.warn("AccountRepository not configured; skipping syncAccounts");
            return;
        }

        try {
            UUID userUuid = UUID.fromString(userId);
            List<Account> existingAccounts = accountRepository.findByUserId(userUuid);
            Map<String, Account> existingByUnipileId = new HashMap<>();
            for (Account acc : existingAccounts) {
                if (acc.getUnipileAccountId() != null) {
                    existingByUnipileId.put(acc.getUnipileAccountId(), acc);
                }
            }

            for (UnipileAccount unipileAccount : accounts) {
                String unipileId = unipileAccount.getId();
                if (unipileId == null || unipileId.isEmpty()) {
                    log.warn("Skipping account without unipile_account_id: {}", unipileId);
                    continue;
                }

                Optional<Account> existingOpt = Optional.ofNullable(existingByUnipileId.get(unipileId));

                if (existingOpt.isPresent()) {
                    Account existing = existingOpt.get();
                    existing.setName(unipileAccount.getName());
                    existing.setStatus(mapAccountStatus(unipileAccount));
                    existing.setInitials(extractInitials(unipileAccount.getName()));
                    existing.setUnipileAccountId(unipileAccount.getId());
                    accountRepository.save(existing);
                    log.info("Updated account in database: {}", unipileId);
                } else {
                    Account newAccount = Account.builder()
                            .userId(userUuid)
                            .name(unipileAccount.getName())
                            .status(mapAccountStatus(unipileAccount))
                            .initials(extractInitials(unipileAccount.getName()))
                            .unipileAccountId(unipileAccount.getId())
                            .build();
                    accountRepository.save(newAccount);
                    log.info("Created new account in database: {}", unipileId);
                }
            }

            List<String> unipileIds = accounts.stream()
                    .map(UnipileAccount::getId)
                    .filter(id -> id != null && !id.isEmpty())
                    .toList();

            for (Account existing : existingAccounts) {
                if (existing.getUnipileAccountId() != null && !unipileIds.contains(existing.getUnipileAccountId())) {
                    accountRepository.delete(existing);
                    log.info("Deleted account no longer in Unipile: {}", existing.getUnipileAccountId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to sync accounts", e);
        }
    }

    public void syncSingleAccount(UnipileAccount account, String userId) {
        if (accountRepository == null) {
            log.warn("AccountRepository not configured; skipping syncSingleAccount");
            return;
        }
        try {
            UUID userUuid = UUID.fromString(userId);
            String unipileId = account.getId();

            Optional<Account> existingOpt = accountRepository.findByUnipileAccountId(unipileId);

            if (existingOpt.isPresent()) {
                Account existing = existingOpt.get();
                existing.setName(account.getName());
                existing.setStatus(mapAccountStatus(account));
                existing.setInitials(extractInitials(account.getName()));
                accountRepository.save(existing);
                log.info("Updated account in database: {}", unipileId);
            } else {
                Account newAccount = Account.builder()
                        .userId(userUuid)
                        .name(account.getName())
                        .status(mapAccountStatus(account))
                        .initials(extractInitials(account.getName()))
                        .unipileAccountId(account.getId())
                        .build();
                accountRepository.save(newAccount);
                log.info("Created new account in database: {}", unipileId);
            }
        } catch (Exception e) {
            log.error("Failed to sync single account", e);
        }
    }

    private String mapStatusFromCallback(String callbackStatus) {
        if (callbackStatus == null) return "Ativo";
        return switch (callbackStatus.toLowerCase()) {
            case "creation_success", "connected" -> "Ativo";
            case "creation_failed" -> "Desconectado";
            default -> "Ativo";
        };
    }

    private String mapAccountStatus(UnipileAccount account) {
        if (account == null) return null;
        String provider = account.getType();
        if (provider == null) return null;

        return switch (provider.toLowerCase()) {
            case "whatsapp", "gmail", "google", "imap",
                 "office365", "outlook", "linkedin", "instagram", "twitter", "x" -> "Ativo";
            default -> "Desconectado";
        };
    }

    private String extractInitials(String name) {
        if (name == null || name.isEmpty()) return null;

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                sb.append(parts[i].charAt(0));
            }
        }
        return sb.toString().toUpperCase();
    }
}