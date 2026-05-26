package com.gugugaga.jsmedicine.module.auth.app.service;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import darabonba.core.client.ClientOverrideConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AliyunSmsCodeSender {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsCodeSender.class);
    private static final long SMS_CODE_LENGTH = 6L;
    private static final String SMS_CODE_TEMPLATE_PLACEHOLDER = "##code##";

    private final AppAuthProperties appAuthProperties;
    private final ObjectMapper objectMapper;

    public AliyunSmsCodeSender(AppAuthProperties appAuthProperties, ObjectMapper objectMapper) {
        this.appAuthProperties = appAuthProperties;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return isConfigured(appAuthProperties.getSms().getAliyun());
    }

    public String sendCode(String mobile, long ttlSeconds) {
        AppAuthProperties.Aliyun aliyun = appAuthProperties.getSms().getAliyun();
        validateConfig(aliyun);
        StaticCredentialProvider credentialProvider = StaticCredentialProvider.create(Credential.builder()
                .accessKeyId(aliyun.getAccessKeyId())
                .accessKeySecret(aliyun.getAccessKeySecret())
                .build());
        try (AsyncClient client = AsyncClient.builder()
                .region(aliyun.getRegionId())
                .credentialsProvider(credentialProvider)
                .overrideConfiguration(ClientOverrideConfiguration.create()
                        .setEndpointOverride(aliyun.getEndpoint()))
                .build()) {
            SendSmsVerifyCodeRequest request = SendSmsVerifyCodeRequest.builder()
                    .signName(aliyun.getSignName())
                    .templateCode(aliyun.getTemplateCode())
                    .phoneNumber(mobile)
                    .templateParam(objectMapper.writeValueAsString(Map.of(
                            "code", SMS_CODE_TEMPLATE_PLACEHOLDER,
                            "min", String.valueOf(Math.max(1, ttlSeconds / 60))
                    )))
                    .codeLength(SMS_CODE_LENGTH)
                    .returnVerifyCode(true)
                    .build();
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCode(request).get();
            SendSmsVerifyCodeResponseBody body = response == null ? null : response.getBody();
            if (body == null || !Boolean.TRUE.equals(body.getSuccess())) {
                String providerCode = body == null
                        ? "NO_RESPONSE"
                        : body.getCode();
                String providerMessage = body == null
                        ? "Aliyun sms returned empty response"
                        : body.getMessage();
                log.warn(
                        "Aliyun sms send failed providerCode={} providerMessage={} signName={} templateCode={} regionId={} endpoint={}",
                        providerCode,
                        providerMessage,
                        aliyun.getSignName(),
                        aliyun.getTemplateCode(),
                        aliyun.getRegionId(),
                        aliyun.getEndpoint()
                );
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to send sms verification code");
            }
            String verifyCode = body.getModel() == null ? null : body.getModel().getVerifyCode();
            if (verifyCode == null || verifyCode.isBlank()) {
                log.warn("Aliyun sms send succeeded but verify code was not returned requestId={}", body.getRequestId());
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to send sms verification code");
            }
            return verifyCode;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.warn("Aliyun sms send failed exceptionType={} exceptionMessage={} signName={} templateCode={} regionId={} endpoint={}",
                    exception.getClass().getSimpleName(),
                    exception.getMessage(),
                    aliyun.getSignName(),
                    aliyun.getTemplateCode(),
                    aliyun.getRegionId(),
                    aliyun.getEndpoint());
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to send sms verification code");
        }
    }

    private void validateConfig(AppAuthProperties.Aliyun aliyun) {
        if (!isConfigured(aliyun)) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Aliyun sms service is not configured");
        }
    }

    private boolean isConfigured(AppAuthProperties.Aliyun aliyun) {
        return !isBlank(aliyun.getAccessKeyId())
                && !isBlank(aliyun.getAccessKeySecret())
                && !isBlank(aliyun.getRegionId())
                && !isBlank(aliyun.getEndpoint())
                && !isBlank(aliyun.getSignName())
                && !isBlank(aliyun.getTemplateCode());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
