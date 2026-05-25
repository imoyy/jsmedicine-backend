package com.gugugaga.jsmedicine.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationAudit {

    String targetType();

    String targetId();

    String afterStatus();

    String beforeStatus() default "";

    String comment() default "";
}
