package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileChat {

    private String object;
    private String id;
    private String account_id;
    private String account_type;
    private String provider_id;
    private String attendee_provider_id;
    private String name;
    private Integer type;
    private String timestamp;
    private Integer unread_count;
    private Integer archived;
    private Object muted_until;
    private Integer read_only;
    private String[] disabledFeatures;
    private String subject;
    private String organization_id;
    private String mailbox_id;
    private String content_type;
    private String[] folder;
    private Integer pinned;
    private LastMessage lastMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LastMessage {
        private String message_id;
        private String provider_id;
        private String sender_id;
        private String text;
        private List<Attachment> attachments;
        private String id;
        private String chat_id;
        private String chat_provider_id;
        private String timestamp;
        private Integer is_sender;
        private Quoted quoted;
        private List<Reaction> reactions;
        private Integer seen;
        private Map<String, Object> seen_by;
        private Integer hidden;
        private Integer deleted;
        private Integer edited;
        private Integer is_event;
        private Integer delivered;
        private Integer behavior;
        private String event_type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        private String id;
        private Number file_size;
        private Boolean unavailable;
        private String mimetype;
        private String url;
        private Number url_expires_at;
        private String type;
        private AttachmentSize size;
        private Boolean sticker;
        private Boolean gif;
        private Number duration;
        private Boolean voice_note;
        private String file_name;
        private Number starts_at;
        private Number expires_at;
        private Number time_range;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AttachmentSize {
        private Number width;
        private Number height;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Quoted {
        private String message_id;
        private String provider_id;
        private String sender_id;
        private String text;
        private List<Attachment> attachments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reaction {
        private String value;
        private String sender_id;
        private Boolean is_sender;
    }
}
