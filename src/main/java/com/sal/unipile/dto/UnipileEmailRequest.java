package com.sal.unipile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnipileEmailRequest {
    private List<EmailRecipient> to;
    private List<EmailRecipient> cc;
    private List<EmailRecipient> bcc;
    private String subject;
    private String body;
    private Boolean draft;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailRecipient {
        private String display_name;
        private String address;
    }
}
