package com.gugugaga.jsmedicine.module.auth.admin.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private long tokenTtlSeconds = 7200;
    private String tokenPrefix = "admin:token:";
    private String bootstrapUsername = "superadmin";
    private String bootstrapPassword;
    private String bootstrapRealName = "系统超级管理员";

    public long getTokenTtlSeconds() {
        return tokenTtlSeconds;
    }

    public void setTokenTtlSeconds(long tokenTtlSeconds) {
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getBootstrapUsername() {
        return bootstrapUsername;
    }

    public void setBootstrapUsername(String bootstrapUsername) {
        this.bootstrapUsername = bootstrapUsername;
    }

    public String getBootstrapPassword() {
        return bootstrapPassword;
    }

    public void setBootstrapPassword(String bootstrapPassword) {
        this.bootstrapPassword = bootstrapPassword;
    }

    public String getBootstrapRealName() {
        return bootstrapRealName;
    }

    public void setBootstrapRealName(String bootstrapRealName) {
        this.bootstrapRealName = bootstrapRealName;
    }
}

