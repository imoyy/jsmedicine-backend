package com.gugugaga.jsmedicine.module.learning.live;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.util.UriComponentsBuilder;

@ConfigurationProperties(prefix = "app.live")
public class LiveStreamingProperties {

    private boolean enabled = true;
    private String mediaHost = "127.0.0.1";
    private int rtmpPort = 1935;
    private int httpPort = 8080;
    private String appName = "live";
    private String callbackBaseUrl = "http://127.0.0.1:8080";
    private String callbackPath = "/api/v1/integrations/srs/live-hooks";
    private String callbackToken;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMediaHost() {
        return mediaHost;
    }

    public void setMediaHost(String mediaHost) {
        this.mediaHost = mediaHost;
    }

    public int getRtmpPort() {
        return rtmpPort;
    }

    public void setRtmpPort(int rtmpPort) {
        this.rtmpPort = rtmpPort;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCallbackBaseUrl() {
        return callbackBaseUrl;
    }

    public void setCallbackBaseUrl(String callbackBaseUrl) {
        this.callbackBaseUrl = callbackBaseUrl;
    }

    public String getCallbackPath() {
        return callbackPath;
    }

    public void setCallbackPath(String callbackPath) {
        this.callbackPath = callbackPath;
    }

    public String getCallbackToken() {
        return callbackToken;
    }

    public void setCallbackToken(String callbackToken) {
        this.callbackToken = callbackToken;
    }

    public String buildPublishUrl(String streamName) {
        return String.format("rtmp://%s:%d/%s/%s", mediaHost, rtmpPort, appName, streamName);
    }

    public String buildHttpFlvUrl(String streamName) {
        return String.format("http://%s:%d/%s/%s.flv", mediaHost, httpPort, appName, streamName);
    }

    public String buildHlsUrl(String streamName) {
        return String.format("http://%s:%d/%s/%s.m3u8", mediaHost, httpPort, appName, streamName);
    }

    public String buildCallbackUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(callbackBaseUrl).path(callbackPath);
        if (callbackToken != null && !callbackToken.isBlank()) {
            builder.queryParam("token", callbackToken);
        }
        return builder.toUriString();
    }
}
