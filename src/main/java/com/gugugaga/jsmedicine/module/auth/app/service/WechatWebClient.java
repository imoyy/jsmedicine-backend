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
public class WechatWebClient {

    private static final Logger log = LoggerFactory.getLogger(WechatWebClient.class);
    private static final String OAUTH_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/access_token";

    private final AppAuthProperties appAuthProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WechatWebClient(
            AppAuthProperties appAuthProperties,
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper
    ) {
        this.appAuthProperties = appAuthProperties;
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = objectMapper;
    }

    public WechatSession codeToSession(String code) {
        AppAuthProperties.WechatWeb wechatWeb = appAuthProperties.getWechatWeb();
        if (wechatWeb.isMockEnabled()) {
            return new WechatSession("mock-web-openid-" + code, "mock-unionid-" + code);
        }
        validateConfig(wechatWeb);
        String url = UriComponentsBuilder.fromHttpUrl(OAUTH_ACCESS_TOKEN_URL)
                .queryParam("appid", wechatWeb.getAppId())
                .queryParam("secret", wechatWeb.getAppSecret())
                .queryParam("code", code)
                .queryParam("grant_type", "authorization_code")
                .toUriString();
        try {
            String responseBody = restTemplate.getForObject(url, String.class);
            WechatWebAccessTokenResponse response = objectMapper.readValue(responseBody, WechatWebAccessTokenResponse.class);
            if (response == null || response.errcode() != null) {
                if (response != null) {
                    log.info("WeChat web oauth rejected request errcode={} errmsg={}", response.errcode(), response.errmsg());
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
            log.warn("Failed to parse WeChat web oauth response", exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to request WeChat authorization service");
        } catch (RestClientException exception) {
            log.warn("Failed to request WeChat web oauth API", exception);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to request WeChat authorization service");
        }
    }

    public void validateConfig() {
        validateConfig(appAuthProperties.getWechatWeb());
    }

    private void validateConfig(AppAuthProperties.WechatWeb wechatWeb) {
        if (isBlank(wechatWeb.getAppId()) || isBlank(wechatWeb.getAppSecret()) || isBlank(wechatWeb.getRedirectUri())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "WeChat website login service is not configured");
        }
        if (isBlank(wechatWeb.getScope())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "WeChat website login scope is not configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record WechatSession(String openId, String unionId) {
    }

    private record WechatWebAccessTokenResponse(
            @JsonProperty("access_token")
            String accessToken,
            @JsonProperty("refresh_token")
            String refreshToken,
            String openid,
            String unionid,
            String scope,
            Integer errcode,
            String errmsg
    ) {
    }
}
