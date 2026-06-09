package com.gugugaga.jsmedicine.module.learning.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("courses")
@EqualsAndHashCode(callSuper = true)
public class Course extends ReviewableEntity {
    private String courseName;
    private String subtitle;
    private String coverUrl;
    private Long coverFileAssetId;
    private String lecturerName;
    private String lecturerAvatarUrl;
    private Long lecturerAvatarFileAssetId;
    private String introduction;
    private Long paperId;
    private Integer sortOrder;
}

