package com.gugugaga.jsmedicine.module.interaction.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppFeedbackRequest(
        @Size(max = 32, message = "feedbackType length must be less than 32")
        @Schema(description = "反馈分类，当前支持前端按页面自行约定传值；后端仅按自由文本存储，不做固定枚举限制", example = "功能建议")
        String feedbackType,

        @NotBlank(message = "content must not be blank")
        String content,

        @Size(max = 128, message = "contact length must be less than 128")
        @Schema(description = "主联系方式，可填写手机号、微信号、邮箱等一种便于联系的信息", example = "wechat_td_user")
        String contact
) {
}
