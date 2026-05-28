package com.gugugaga.jsmedicine.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationResponse;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeResponse;
import com.gugugaga.jsmedicine.module.user.entity.Organization;
import com.gugugaga.jsmedicine.module.user.entity.PracticeType;
import com.gugugaga.jsmedicine.module.user.mapper.OrganizationMapper;
import com.gugugaga.jsmedicine.module.user.mapper.PracticeTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminReferenceService {

    private final OrganizationMapper organizationMapper;
    private final PracticeTypeMapper practiceTypeMapper;

    public AdminReferenceService(OrganizationMapper organizationMapper, PracticeTypeMapper practiceTypeMapper) {
        this.organizationMapper = organizationMapper;
        this.practiceTypeMapper = practiceTypeMapper;
    }

    public List<OrganizationResponse> organizations(String keyword, String provinceCode, String cityCode,
                                                    String districtCode, EnabledStatus status) {
        return organizationMapper.selectList(new LambdaQueryWrapper<Organization>()
                        .eq(Organization::getDeleted, 0)
                        .eq(status != null, Organization::getStatus, status)
                        .eq(hasText(provinceCode), Organization::getProvinceCode, provinceCode)
                        .eq(hasText(cityCode), Organization::getCityCode, cityCode)
                        .eq(hasText(districtCode), Organization::getDistrictCode, districtCode)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Organization::getOrgName, keyword)
                                .or()
                                .like(Organization::getOrgCode, keyword))
                        .orderByAsc(Organization::getSortOrder)
                        .orderByAsc(Organization::getId))
                .stream()
                .map(this::toOrganizationResponse)
                .toList();
    }

    public List<PracticeTypeResponse> practiceTypes(Long parentId, EnabledStatus status) {
        return practiceTypeMapper.selectList(new LambdaQueryWrapper<PracticeType>()
                        .eq(PracticeType::getDeleted, 0)
                        .eq(parentId != null, PracticeType::getParentId, parentId)
                        .eq(status != null, PracticeType::getStatus, status)
                        .orderByAsc(PracticeType::getSortOrder)
                        .orderByAsc(PracticeType::getId))
                .stream()
                .map(this::toPracticeTypeResponse)
                .toList();
    }

    private OrganizationResponse toOrganizationResponse(Organization organization) {
        return new OrganizationResponse(
                organization.getId(),
                organization.getOrgCode(),
                organization.getOrgName(),
                organization.getOrgType(),
                organization.getProvinceCode(),
                organization.getCityCode(),
                organization.getDistrictCode(),
                organization.getAddress(),
                organization.getStatus(),
                organization.getSortOrder()
        );
    }

    private PracticeTypeResponse toPracticeTypeResponse(PracticeType practiceType) {
        return new PracticeTypeResponse(
                practiceType.getId(),
                practiceType.getParentId(),
                practiceType.getTypeCode(),
                practiceType.getTypeName(),
                practiceType.getStatus(),
                practiceType.getSortOrder()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
