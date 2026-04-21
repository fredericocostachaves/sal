package com.sal.unipile.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column
    private String status;

    @Column
    private String initials;

    @Column(name = "proxy_settings", columnDefinition = "jsonb")
    private String proxySettings;

    @Transient
    private Map<String, Object> proxySettingsMap;

    public Map<String, Object> getProxySettings() {
        if (proxySettings == null) return Map.of();
        try {
            return new ObjectMapper().readValue(proxySettings, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    public void setProxySettings(Map<String, Object> map) {
        try {
            this.proxySettings = new ObjectMapper().writeValueAsString(map);
            this.proxySettingsMap = map;
        } catch (JsonProcessingException e) {
            this.proxySettings = "{}";
        }
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "unipile_account_id")
    private String unipileAccountId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
