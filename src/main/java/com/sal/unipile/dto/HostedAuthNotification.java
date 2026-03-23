package com.sal.unipile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostedAuthNotification {
    private String status; // 'CREATION_SUCCESS' | 'RECONNECTED'
    private String account_id;
    private String name; // internal user ID
}
