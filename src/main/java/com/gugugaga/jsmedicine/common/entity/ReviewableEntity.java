package com.gugugaga.jsmedicine.common.entity;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class ReviewableEntity extends ManagedEntity {
    private ReviewStatus reviewStatus;
    private PublishStatus publishStatus;
    private LocalDateTime publishedAt;
}
