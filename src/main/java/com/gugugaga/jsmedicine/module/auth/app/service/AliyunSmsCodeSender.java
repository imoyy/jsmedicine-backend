package com.gugugaga.jsmedicine.module.auth.app.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AliyunSmsCodeSender {

    private static final String SUCCESS_CODE = "OK";

    private final AppAuthProperties appAuthProperties;
    private final ObjectMapper objectMapper;

    public AliyunSmsCodeSender(AppAuthProperties appAuthProperties, ObjectMapper objectMapper) {
        this.appAuthProperties = appAuthProperties;
        this.objectMapper = objectMapper;
    }

    public void sendCode(String mobile, String code) {
        AppAuthProperties.Aliyun aliyun = appAuthProperties.getSms().getAliyun();
        validateConfig(aliyun);
        try {
            Client client = new Client(new Config()
                    .setAccessKeyId(aliyun.getAccessKeyId())
                    .setAccessKeySecret(aliyun.getAccessKeySecret())
                    .setEndpoint(aliyun.getEndpoint()));
            SendSmsRequest request = new SendSmsRequest()
                    .setPhoneNumbers(mobile)
                    .setSignName(aliyun.getSignName())
                    .setTemplateCode(aliyun.getTemplateCode())
                    .setTemplateParam(objectMapper.writeValueAsString(Map.of("code", code)));
            SendSmsResponse response = client.sendSms(request);
            if (response == null || response.getBody() == null || !SUCCESS_CODE.equals(response.getBody().getCode())) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to send sms verification code");
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to send sms verification code");
        }
    }

    private void validateConfig(AppAuthProperties.Aliyun aliyun) {
        if (isBlank(aliyun.getAccessKeyId())
                || isBlank(aliyun.getAccessKeySecret())
                || isBlank(aliyun.getEndpoint())
                || isBlank(aliyun.getSignName())
                || isBlank(aliyun.getTemplateCode())) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Aliyun sms service is not configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
