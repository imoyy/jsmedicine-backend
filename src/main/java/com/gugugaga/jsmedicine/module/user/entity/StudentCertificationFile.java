package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("student_certification_files")
@EqualsAndHashCode(callSuper = true)
public class StudentCertificationFile extends ManagedEntity {
    private Long studentId;
    private Long fileAssetId;
    private String sourceUrl;
    private String materialType;
    private Integer sortOrder;
}
