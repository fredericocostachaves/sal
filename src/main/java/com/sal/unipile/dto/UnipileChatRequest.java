package com.sal.unipile.dto;

import lombok.Data;
import java.util.List;

@Data
public class UnipileChatRequest {
    private List<String> attendees_ids;
    private String text;
}
