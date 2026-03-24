package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileAccountResponse {
    private String object;
    private List<UnipileAccount> items;
    private String cursor;
}
