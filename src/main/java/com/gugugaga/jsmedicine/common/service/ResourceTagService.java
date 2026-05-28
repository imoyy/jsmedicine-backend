package com.gugugaga.jsmedicine.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.entity.ResourceTag;
import com.gugugaga.jsmedicine.common.entity.Tag;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.mapper.ResourceTagMapper;
import com.gugugaga.jsmedicine.common.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ResourceTagService {

    private final TagMapper tagMapper;
    private final ResourceTagMapper resourceTagMapper;

    public ResourceTagService(TagMapper tagMapper, ResourceTagMapper resourceTagMapper) {
        this.tagMapper = tagMapper;
        this.resourceTagMapper = resourceTagMapper;
    }

    public List<String> loadTagNames(String resourceType, Long resourceId) {
        List<ResourceTag> relations = resourceTagMapper.selectList(new LambdaQueryWrapper<ResourceTag>()
                .eq(ResourceTag::getResourceType, resourceType)
                .eq(ResourceTag::getResourceId, resourceId)
                .orderByAsc(ResourceTag::getId));
        if (relations.isEmpty()) {
            return List.of();
        }
        Map<Long, Integer> orderMap = new LinkedHashMap<>();
        relations.forEach(relation -> orderMap.put(relation.getTagId(), orderMap.size()));
        return tagMapper.selectBatchIds(orderMap.keySet()).stream()
                .filter(Objects::nonNull)
                .sorted((left, right) -> Integer.compare(orderMap.getOrDefault(left.getId(), Integer.MAX_VALUE),
                        orderMap.getOrDefault(right.getId(), Integer.MAX_VALUE)))
                .map(Tag::getTagName)
                .filter(Objects::nonNull)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void replaceTags(String resourceType, Long resourceId, List<String> tagNames) {
        resourceTagMapper.delete(new LambdaQueryWrapper<ResourceTag>()
                .eq(ResourceTag::getResourceType, resourceType)
                .eq(ResourceTag::getResourceId, resourceId));
        if (tagNames == null || tagNames.isEmpty()) {
            return;
        }
        tagNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .forEach(tagName -> bindTag(resourceType, resourceId, tagName));
    }

    private void bindTag(String resourceType, Long resourceId, String tagName) {
        Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagType, resourceType)
                .eq(Tag::getTagName, tagName)
                .eq(Tag::getDeleted, 0)
                .last("LIMIT 1"));
        if (tag == null) {
            tag = new Tag();
            tag.setTagName(tagName);
            tag.setTagType(resourceType);
            tag.setStatus(EnabledStatus.ENABLED);
            tag.setDeleted(0);
            tagMapper.insert(tag);
        }
        ResourceTag relation = new ResourceTag();
        relation.setTagId(tag.getId());
        relation.setResourceType(resourceType);
        relation.setResourceId(resourceId);
        resourceTagMapper.insert(relation);
    }
}
