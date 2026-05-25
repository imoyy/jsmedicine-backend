package com.gugugaga.jsmedicine.common.config;

import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationAuditAspect {

    private static final long SYSTEM_OPERATOR_ID = 0L;

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;

    public OperationAuditAspect(AuditRecordService auditRecordService, CurrentAdminAccessor currentAdminAccessor) {
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    @Around("@annotation(operationAudit)")
    public Object auditOperation(ProceedingJoinPoint joinPoint, OperationAudit operationAudit) throws Throwable {
        Object result = joinPoint.proceed();
        AuditRecord auditRecord = buildAuditRecord(joinPoint, operationAudit, result);
        auditRecordService.save(auditRecord);
        return result;
    }

    private AuditRecord buildAuditRecord(
            ProceedingJoinPoint joinPoint,
            OperationAudit operationAudit,
            Object result
    ) {
        EvaluationContext context = buildEvaluationContext(joinPoint, result);
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setTargetType(operationAudit.targetType());
        auditRecord.setTargetId(resolveLong(operationAudit.targetId(), context, "targetId"));
        auditRecord.setBeforeStatus(resolveInteger(operationAudit.beforeStatus(), context));
        Integer afterStatus = resolveInteger(operationAudit.afterStatus(), context);
        if (afterStatus == null) {
            throw new IllegalArgumentException("Operation audit afterStatus must not be null");
        }
        auditRecord.setAfterStatus(afterStatus);
        auditRecord.setAuditComment(resolveString(operationAudit.comment(), context));
        auditRecord.setAuditorId(currentAdminAccessor.getCurrentAdminId().orElse(SYSTEM_OPERATOR_ID));
        auditRecord.setAuditedAt(LocalDateTime.now());
        return auditRecord;
    }

    private EvaluationContext buildEvaluationContext(ProceedingJoinPoint joinPoint, Object result) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Object[] arguments = joinPoint.getArgs();
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], arguments[i]);
            }
        }
        context.setVariable("args", arguments);
        context.setVariable("result", result);
        context.setVariable("operatorId", currentAdminAccessor.getCurrentAdminId().orElse(SYSTEM_OPERATOR_ID));
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        context.setVariable("request", request);
        return context;
    }

    private Long resolveLong(String expression, EvaluationContext context, String fieldName) {
        Object value = evaluate(expression, context);
        if (value == null) {
            throw new IllegalArgumentException("Operation audit " + fieldName + " must not be null");
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer resolveInteger(String expression, EvaluationContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        Object value = evaluate(expression, context);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private String resolveString(String expression, EvaluationContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        Object value = evaluate(expression, context);
        return value == null ? null : String.valueOf(value);
    }

    private Object evaluate(String expression, EvaluationContext context) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        return expressionParser.parseExpression(expression).getValue(context);
    }
}
