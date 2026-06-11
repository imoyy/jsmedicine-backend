package com.gugugaga.jsmedicine.module.learning.live;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.web.util.UriComponentsBuilder;

@ConfigurationProperties(prefix = "app.live")
public class LiveStreamingProperties {

    private boolean enabled = true;
    private String mediaHost = "127.0.0.1";
    private String publishHost;
    private String playbackHost;
    private String playbackBaseUrl;
    private String playbackScheme = "http";
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

    public String getPublishHost() {
        return publishHost;
    }

    public void setPublishHost(String publishHost) {
        this.publishHost = publishHost;
    }

    public String getPlaybackHost() {
        return playbackHost;
    }

    public void setPlaybackHost(String playbackHost) {
        this.playbackHost = playbackHost;
    }

    public String getPlaybackBaseUrl() {
        return playbackBaseUrl;
    }

    public void setPlaybackBaseUrl(String playbackBaseUrl) {
        this.playbackBaseUrl = playbackBaseUrl;
    }

    public String getPlaybackScheme() {
        return playbackScheme;
    }

    public void setPlaybackScheme(String playbackScheme) {
        this.playbackScheme = playbackScheme;
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
        return String.format("rtmp://%s:%d/%s/%s", resolvedPublishHost(), rtmpPort, appName, streamName);
    }

    public String buildHttpFlvUrl(String streamName) {
        if (hasText(playbackBaseUrl)) {
            return buildPlaybackUrlFromBase(streamName, "flv");
        }
        return String.format("%s://%s:%d/%s/%s.flv", resolvedPlaybackScheme(), resolvedPlaybackHost(), httpPort, appName, streamName);
    }

    public String buildHlsUrl(String streamName) {
        if (hasText(playbackBaseUrl)) {
            return buildPlaybackUrlFromBase(streamName, "m3u8");
        }
        return String.format("%s://%s:%d/%s/%s.m3u8", resolvedPlaybackScheme(), resolvedPlaybackHost(), httpPort, appName, streamName);
    }

    public String buildCallbackUrl() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(callbackBaseUrl).path(callbackPath);
        if (callbackToken != null && !callbackToken.isBlank()) {
            builder.queryParam("token", callbackToken);
        }
        return builder.toUriString();
    }

    private String resolvedPublishHost() {
        return hasText(publishHost) ? publishHost.trim() : mediaHost;
    }

    private String resolvedPlaybackHost() {
        return hasText(playbackHost) ? playbackHost.trim() : mediaHost;
    }

    private String resolvedPlaybackScheme() {
        return hasText(playbackScheme) ? playbackScheme.trim() : "http";
    }

    private String buildPlaybackUrlFromBase(String streamName, String extension) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(playbackBaseUrl.trim());
        if (!playbackBaseUrlContainsAppName()) {
            builder.pathSegment(appName);
        }
        return builder
                .pathSegment(streamName + "." + extension)
                .build()
                .toUriString();
    }

    private boolean playbackBaseUrlContainsAppName() {
        String path = UriComponentsBuilder.fromUriString(playbackBaseUrl.trim()).build().getPath();
        if (!hasText(path)) {
            return false;
        }
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;
        return normalizedPath.equals("/" + appName) || normalizedPath.endsWith("/" + appName);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
