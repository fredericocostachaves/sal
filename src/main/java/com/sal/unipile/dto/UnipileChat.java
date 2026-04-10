package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Representa um chat no Unipile")
public class UnipileChat {

    @Schema(description = "Tipo de objeto", example = "chat")
    private String object;

    @Schema(description = "ID único do chat", example = "CHT_12345")
    private String id;

    @Schema(description = "ID da conta associada", example = "ACC_67890")
    private String account_id;

    @Schema(description = "Tipo de conta (ex: LINKEDIN)", example = "LINKEDIN")
    private String account_type;

    @Schema(description = "ID do chat no provedor", example = "li_chat_123")
    private String provider_id;

    @Schema(description = "ID do participante no provedor")
    private String attendee_provider_id;

    @Schema(description = "Nome do chat ou do participante", example = "John Doe")
    private String name;

    @Schema(description = "Tipo de chat (1 para direto, 2 para grupo)", example = "1")
    private Integer type;

    @Schema(description = "Timestamp da última atividade", example = "2023-10-27T10:00:00Z")
    private String timestamp;

    @Schema(description = "Contagem de mensagens não lidas", example = "0")
    private Integer unread_count;

    @Schema(description = "Indica se o chat está arquivado (1 para sim, 0 para não)", example = "0")
    private Integer archived;

    @Schema(description = "Data até a qual o chat está silenciado")
    private Object muted_until;

    @Schema(description = "Indica se o chat é apenas leitura (1 para sim, 0 para não)", example = "0")
    private Integer read_only;

    @Schema(description = "Lista de funcionalidades desabilitadas")
    private String[] disabledFeatures;

    @Schema(description = "Assunto do chat (principalmente para e-mails)")
    private String subject;

    @Schema(description = "ID da organização")
    private String organization_id;

    @Schema(description = "ID da caixa de correio")
    private String mailbox_id;

    @Schema(description = "Tipo de conteúdo")
    private String content_type;

    @Schema(description = "Pastas associadas")
    private String[] folder;

    @Schema(description = "Indica se o chat está fixado (1 para sim, 0 para não)", example = "0")
    private Integer pinned;

    @Schema(description = "Última mensagem recebida ou enviada")
    private LastMessage lastMessage;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa a última mensagem de um chat")
    public static class LastMessage {
        @Schema(description = "ID único da mensagem", example = "MSG_12345")
        private String message_id;

        @Schema(description = "ID da mensagem no provedor")
        private String provider_id;

        @Schema(description = "ID do remetente", example = "SND_12345")
        private String sender_id;

        @Schema(description = "Texto da mensagem", example = "Olá, tudo bem?")
        private String text;

        @Schema(description = "Lista de anexos da mensagem")
        private List<Attachment> attachments;

        @Schema(description = "ID da mensagem")
        private String id;

        @Schema(description = "ID do chat associado")
        private String chat_id;

        @Schema(description = "ID do chat no provedor")
        private String chat_provider_id;

        @Schema(description = "Timestamp da mensagem", example = "2023-10-27T10:00:00Z")
        private String timestamp;

        @Schema(description = "Indica se a mensagem foi enviada pelo usuário (1 para sim, 0 para não)", example = "1")
        private Integer is_sender;

        @Schema(description = "Mensagem citada, se houver")
        private Quoted quoted;

        @Schema(description = "Reações à mensagem")
        private List<Reaction> reactions;

        @Schema(description = "Indica se a mensagem foi vista (1 para sim, 0 para não)", example = "1")
        private Integer seen;

        @Schema(description = "Mapa de quem viu a mensagem")
        private Map<String, Object> seen_by;

        @Schema(description = "Indica se a mensagem está oculta", example = "0")
        private Integer hidden;

        @Schema(description = "Indica se a mensagem foi deletada", example = "0")
        private Integer deleted;

        @Schema(description = "Indica se a mensagem foi editada", example = "0")
        private Integer edited;

        @Schema(description = "Indica se é um evento", example = "0")
        private Integer is_event;

        @Schema(description = "Indica se foi entregue", example = "1")
        private Integer delivered;

        @Schema(description = "Comportamento da mensagem")
        private Integer behavior;

        @Schema(description = "Tipo de evento")
        private String event_type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa um anexo de mensagem")
    public static class Attachment {
        @Schema(description = "ID único do anexo", example = "ATT_12345")
        private String id;

        @Schema(description = "Tamanho do arquivo em bytes", example = "1024")
        private Number file_size;

        @Schema(description = "Indica se o anexo está indisponível", example = "false")
        private Boolean unavailable;

        @Schema(description = "Tipo MIME do arquivo", example = "image/jpeg")
        private String mimetype;

        @Schema(description = "URL do anexo")
        private String url;

        @Schema(description = "Timestamp de expiração da URL")
        private Number url_expires_at;

        @Schema(description = "Tipo do anexo (ex: image, document)", example = "image")
        private String type;

        @Schema(description = "Dimensões do anexo, se aplicável")
        private AttachmentSize size;

        @Schema(description = "Indica se é um sticker", example = "false")
        private Boolean sticker;

        @Schema(description = "Indica se é um GIF", example = "false")
        private Boolean gif;

        @Schema(description = "Duração em segundos (para áudio/vídeo)", example = "60")
        private Number duration;

        @Schema(description = "Indica se é uma nota de voz", example = "false")
        private Boolean voice_note;

        @Schema(description = "Nome do arquivo", example = "documento.pdf")
        private String file_name;

        @Schema(description = "Timestamp de início")
        private Number starts_at;

        @Schema(description = "Timestamp de expiração")
        private Number expires_at;

        @Schema(description = "Intervalo de tempo")
        private Number time_range;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Dimensões de um anexo")
    public static class AttachmentSize {
        @Schema(description = "Largura em pixels", example = "800")
        private Number width;

        @Schema(description = "Altura em pixels", example = "600")
        private Number height;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa uma mensagem citada")
    public static class Quoted {
        @Schema(description = "ID da mensagem citada", example = "MSG_12345")
        private String message_id;

        @Schema(description = "ID da mensagem citada no provedor")
        private String provider_id;

        @Schema(description = "ID do remetente da mensagem citada")
        private String sender_id;

        @Schema(description = "Texto da mensagem citada")
        private String text;

        @Schema(description = "Lista de anexos da mensagem citada")
        private List<Attachment> attachments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(description = "Representa uma reação a uma mensagem")
    public static class Reaction {
        @Schema(description = "Valor da reação (emoji)", example = "👍")
        private String value;

        @Schema(description = "ID do remetente da reação")
        private String sender_id;

        @Schema(description = "Indica se a reação foi enviada pelo usuário", example = "true")
        private Boolean is_sender;
    }
}
