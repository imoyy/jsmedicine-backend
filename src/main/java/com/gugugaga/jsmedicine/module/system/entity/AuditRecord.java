package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("audit_records")
@EqualsAndHashCode(callSuper = true)
public class AuditRecord extends BaseEntity {
    private String targetType;
    private Long targetId;
    private Integer beforeStatus;
    private Integer afterStatus;
    private String auditComment;
    private Long auditorId;
    private LocalDateTime auditedAt;
}
