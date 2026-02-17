package com.sal.unipile.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UnipileLinkedInSearchResult {
    private String type;
    private String industry;
    private String id;
    private String name;
    private String member_urn;
    private String public_identifier;
    private String profile_url;
    private String public_profile_url;
    private String profile_picture_url;
    private String profile_picture_url_large;
    private String network_distance;
    private String location;
    private String headline;
    private Boolean verified;
    private Boolean premium;
}
