package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileChatAttendee {

    private String object;
    private String id;
    private String account_id;
    private String provider_id;
    private String name;
    private Integer is_self;
    private Integer hidden;
    private String picture_url;
    private String profile_url;
    private Object specifics;
}
