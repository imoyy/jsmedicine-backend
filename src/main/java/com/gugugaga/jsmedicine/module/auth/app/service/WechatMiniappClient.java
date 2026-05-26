package com.gugugaga.jsmedicine.module.auth.app.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WechatMiniappClient {

    private static final String CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final AppAuthProperties appAuthProperties;
    private final RestTemplate restTemplate;

    public WechatMiniappClient(AppAuthProperties appAuthProperties, RestTemplateBuilder restTemplateBuilder) {
        this.appAuthProperties = appAuthProperties;
        this.restTemplate = restTemplateBuilder.build();
    }

    public WechatSession codeToSession(String code) {
        AppAuthProperties.Wechat wechat = appAuthProperties.getWechat();
        if (wechat.isMockEnabled()) {
            return new WechatSession("mock-openid-" + code, "mock-unionid-" + code);
        }
        validateConfig(wechat);
        String url = UriComponentsBuilder.fromHttpUrl(CODE_TO_SESSION_URL)
                .queryParam("appid", wechat.getAppId())
                .queryParam("secret", wechat.getAppSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        try {
            WechatCodeToSessionResponse response = restTemplate.getForObject(url, WechatCodeToSessionResponse.class);
            if (response == null || response.errcode() != null) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat authorization code is invalid");
            }
            if (isBlank(response.openid())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat authorization code is invalid");
            }
            return new WechatSession(response.openid(), response.unionid());
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to request WeChat authorization service");
        }
    }

    private void validateConfig(AppAuthProperties.Wechat wechat) {
        if (isBlank(wechat.getAppId()) || isBlank(wechat.getAppSecret())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "WeChat miniapp service is not configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record WechatSession(String openId, String unionId) {
    }

    private record WechatCodeToSessionResponse(
            String openid,
            @JsonProperty("session_key")
            String sessionKey,
            String unionid,
            Integer errcode,
            String errmsg
    ) {
    }
}
