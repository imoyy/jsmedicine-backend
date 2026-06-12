package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("expert_certification_files")
@EqualsAndHashCode(callSuper = true)
public class ExpertCertificationFile extends ManagedEntity {
    private Long certificationId;
    private Long fileAssetId;
    private String sourceUrl;
    private String materialType;
    private Integer sortOrder;
}
