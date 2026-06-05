package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SrsLiveHookRequest(
        String action,
        String app,
        String stream,
        @JsonProperty("stream_url")
        String streamUrl,
        String param,
        String vhost,
        @JsonProperty("client_id")
        String clientId,
        String ip,
        @JsonProperty("tcUrl")
        String tcUrl,
        @JsonProperty("server_id")
        String serverId
) {
}
