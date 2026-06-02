package com.gugugaga.jsmedicine.module.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record AuditRecordResponse(
        Long id,
        @Schema(description = "审核目标类型编码", example = "article")
        String targetType,
        @Schema(description = "审核目标类型名称", example = "资讯")
        String targetTypeLabel,
        @Schema(description = "状态值所属语义类型，review_status=审核状态，qa_status=答疑状态，feedback_status=反馈状态，login_result=登录结果",
                example = "review_status")
        String statusType,
        Long targetId,
        @Schema(description = "审核前状态值", example = "1")
        Integer beforeStatus,
        @Schema(description = "审核前状态说明", example = "待审核")
        String beforeStatusLabel,
        @Schema(description = "审核后状态值", example = "2")
        Integer afterStatus,
        @Schema(description = "审核后状态说明", example = "已通过")
        String afterStatusLabel,
        String auditComment,
        Long auditorId,
        @Schema(description = "审核人展示名，优先返回真实姓名，缺失时回退管理员账号", example = "系统管理员")
        String auditorName,
        @Schema(description = "审核人管理员账号", example = "td_admin")
        String auditorUsername,
        LocalDateTime auditedAt,
        LocalDateTime createdAt
) {
}
