package com.gugugaga.jsmedicine.module.learning.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("course_videos")
@EqualsAndHashCode(callSuper = true)
public class CourseVideo extends ManagedEntity {
    private Long courseId;
    private String title;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer sortOrder;
    private EnabledStatus status;
}

