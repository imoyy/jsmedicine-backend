package com.gugugaga.jsmedicine.module.auth.app.service;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.user-auth")
public class AppAuthProperties {

    private long tokenTtlSeconds = 7200;
    private String tokenPrefix = "app:user:token:";
    private Sms sms = new Sms();
    private Wechat wechat = new Wechat();

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

    public Sms getSms() {
        return sms;
    }

    public void setSms(Sms sms) {
        this.sms = sms;
    }

    public Wechat getWechat() {
        return wechat;
    }

    public void setWechat(Wechat wechat) {
        this.wechat = wechat;
    }

    public static class Sms {
        private long codeTtlSeconds = 300;
        private String codePrefix = "app:user:sms:";
        private boolean mockEnabled;
        private String mockCode;
        private Aliyun aliyun = new Aliyun();

        public long getCodeTtlSeconds() {
            return codeTtlSeconds;
        }

        public void setCodeTtlSeconds(long codeTtlSeconds) {
            this.codeTtlSeconds = codeTtlSeconds;
        }

        public String getCodePrefix() {
            return codePrefix;
        }

        public void setCodePrefix(String codePrefix) {
            this.codePrefix = codePrefix;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }

        public String getMockCode() {
            return mockCode;
        }

        public void setMockCode(String mockCode) {
            this.mockCode = mockCode;
        }

        public Aliyun getAliyun() {
            return aliyun;
        }

        public void setAliyun(Aliyun aliyun) {
            this.aliyun = aliyun;
        }
    }

    public static class Aliyun {
        private String accessKeyId;
        private String accessKeySecret;
        private String regionId = "ap-southeast-1";
        private String endpoint = "dypnsapi.aliyuncs.com";
        private String signName;
        private String templateCode;

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessKeySecret() {
            return accessKeySecret;
        }

        public void setAccessKeySecret(String accessKeySecret) {
            this.accessKeySecret = accessKeySecret;
        }

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getSignName() {
            return signName;
        }

        public void setSignName(String signName) {
            this.signName = normalizePropertyEncoding(signName);
        }

        public String getTemplateCode() {
            return templateCode;
        }

        public void setTemplateCode(String templateCode) {
            this.templateCode = templateCode;
        }
    }

    public static class Wechat {
        private String appId;
        private String appSecret;
        private boolean mockEnabled;

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getAppSecret() {
            return appSecret;
        }

        public void setAppSecret(String appSecret) {
            this.appSecret = appSecret;
        }

        public boolean isMockEnabled() {
            return mockEnabled;
        }

        public void setMockEnabled(boolean mockEnabled) {
            this.mockEnabled = mockEnabled;
        }
    }

    private static String normalizePropertyEncoding(String value) {
        if (value == null || value.isBlank() || !containsLatin1Supplement(value)) {
            return value;
        }
        String decoded = new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Spring imports extensionless .env files as Java properties. Raw UTF-8 Chinese text can be read as ISO-8859-1.
        return containsCjk(decoded) ? decoded : value;
    }

    private static boolean containsLatin1Supplement(String value) {
        return value.chars().anyMatch(codePoint -> codePoint >= 0x00C0 && codePoint <= 0x00FF);
    }

    private static boolean containsCjk(String value) {
        return value.chars().anyMatch(codePoint -> codePoint >= 0x4E00 && codePoint <= 0x9FFF);
    }
}
