package com.gugugaga.jsmedicine.common.config;

import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class RequestLogAspect {

    private static final Logger log = LoggerFactory.getLogger(RequestLogAspect.class);
    private static final int MAX_BODY_LENGTH = 2048;

    private final CurrentAdminAccessor currentAdminAccessor;

    public RequestLogAspect(CurrentAdminAccessor currentAdminAccessor) {
        this.currentAdminAccessor = currentAdminAccessor;
    }

    @Pointcut("within(com.gugugaga.jsmedicine..*) && @within(org.springframework.web.bind.annotation.RestController)")
    public void restControllerMethods() {
    }

    @Around("restControllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        long startedAt = System.currentTimeMillis();
        Throwable failure = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            writeLog(joinPoint, request, response, startedAt, failure);
        }
    }

    private void writeLog(
            ProceedingJoinPoint joinPoint,
            HttpServletRequest request,
            HttpServletResponse response,
            long startedAt,
            Throwable failure
    ) {
        String requestId = String.valueOf(request.getAttribute(RequestBodyCachingFilter.REQUEST_ID_ATTRIBUTE));
        Long operatorId = currentAdminAccessor.getCurrentAdminId().orElse(null);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        int status = resolveStatus(response, failure);
        long duration = System.currentTimeMillis() - startedAt;
        String requestBody = extractRequestBody(request);
        String responseBody = extractResponseBody(response);
        String parameters = Arrays.stream(signature.getParameterNames() == null ? new String[0] : signature.getParameterNames())
                .collect(Collectors.joining(","));
        log.info(
                "Request handled requestId={} operatorId={} method={} uri={} query={} clientIp={} handler={}.{} args=[{}] status={} durationMs={} requestBody={} responseBody={} error={}",
                requestId,
                operatorId,
                request.getMethod(),
                request.getRequestURI(),
                request.getQueryString(),
                resolveClientIp(request),
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                parameters,
                status,
                duration,
                requestBody,
                responseBody,
                failure == null ? null : failure.getClass().getSimpleName()
        );
    }

    private int resolveStatus(HttpServletResponse response, Throwable failure) {
        if (response != null && response.getStatus() > 0) {
            return response.getStatus();
        }
        return failure == null ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractRequestBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return null;
        }
        byte[] body = wrapper.getContentAsByteArray();
        if (body.length == 0) {
            return null;
        }
        return abbreviate(new String(body, StandardCharsets.UTF_8));
    }

    private String extractResponseBody(HttpServletResponse response) {
        if (!(response instanceof ContentCachingResponseWrapper wrapper)) {
            return null;
        }
        byte[] body = wrapper.getContentAsByteArray();
        if (body.length == 0) {
            return null;
        }
        return abbreviate(new String(body, StandardCharsets.UTF_8));
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= MAX_BODY_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, MAX_BODY_LENGTH) + "...";
    }
}
