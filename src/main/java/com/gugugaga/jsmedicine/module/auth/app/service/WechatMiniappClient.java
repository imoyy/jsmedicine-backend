package com.gugugaga.jsmedicine.module.auth.app.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class WechatMiniappClient {

    private static final Logger log = LoggerFactory.getLogger(WechatMiniappClient.class);
    private static final String CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final AppAuthProperties appAuthProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WechatMiniappClient(
            AppAuthProperties appAuthProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper
    ) {
        this.appAuthProperties = appAuthProperties;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
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
            String responseBody = restTemplate.getForObject(url, String.class);
            WechatCodeToSessionResponse response = objectMapper.readValue(responseBody, WechatCodeToSessionResponse.class);
            if (response == null || response.errcode() != null) {
                if (response != null) {
                    log.info("WeChat code2session rejected request errcode={} errmsg={}", response.errcode(), response.errmsg());
                }
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat authorization code is invalid");
            }
            if (isBlank(response.openid())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "WeChat authorization code is invalid");
            }
            return new WechatSession(response.openid(), response.unionid());
        } catch (BusinessException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            log.warn("Failed to parse WeChat code2session response", exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to request WeChat authorization service");
        } catch (RestClientException exception) {
            log.warn("Failed to request WeChat code2session API", exception);
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
