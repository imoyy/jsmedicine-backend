package com.gugugaga.jsmedicine.module.auth.app.service;

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
        private String endpoint = "dysmsapi.aliyuncs.com";
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
            this.signName = signName;
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
}
