package com.gugugaga.jsmedicine.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationResponse;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationUpsertRequest;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeResponse;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeUpsertRequest;
import com.gugugaga.jsmedicine.module.user.entity.Organization;
import com.gugugaga.jsmedicine.module.user.entity.PracticeType;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.OrganizationMapper;
import com.gugugaga.jsmedicine.module.user.mapper.PracticeTypeMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class AdminReferenceService {

    private final OrganizationMapper organizationMapper;
    private final PracticeTypeMapper practiceTypeMapper;
    private final StudentMapper studentMapper;
    private final ExpertMapper expertMapper;

    public AdminReferenceService(
            OrganizationMapper organizationMapper,
            PracticeTypeMapper practiceTypeMapper,
            StudentMapper studentMapper,
            ExpertMapper expertMapper
    ) {
        this.organizationMapper = organizationMapper;
        this.practiceTypeMapper = practiceTypeMapper;
        this.studentMapper = studentMapper;
        this.expertMapper = expertMapper;
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

    public OrganizationResponse getOrganization(Long id) {
        return toOrganizationResponse(requireOrganization(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public OrganizationResponse createOrganization(OrganizationUpsertRequest request) {
        if (hasText(request.orgCode())) {
            ensureOrganizationCodeAvailable(request.orgCode(), null);
        }
        Organization organization = new Organization();
        fillOrganization(organization, request);
        organization.setDeleted(0);
        organizationMapper.insert(organization);
        return getOrganization(organization.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public OrganizationResponse updateOrganization(Long id, OrganizationUpsertRequest request) {
        Organization organization = requireOrganization(id);
        if (hasText(request.orgCode())) {
            ensureOrganizationCodeAvailable(request.orgCode(), id);
        }
        fillOrganization(organization, request);
        organizationMapper.updateById(organization);
        return getOrganization(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrganization(Long id) {
        requireOrganization(id);
        ensureOrganizationUnused(id);
        organizationMapper.deleteById(id);
    }

    public List<PracticeTypeResponse> practiceTypes(String keyword, Long parentId, EnabledStatus status) {
        return practiceTypeMapper.selectList(new LambdaQueryWrapper<PracticeType>()
                        .eq(PracticeType::getDeleted, 0)
                        .eq(parentId != null, PracticeType::getParentId, parentId)
                        .eq(status != null, PracticeType::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(PracticeType::getTypeName, keyword)
                                .or()
                                .like(PracticeType::getTypeCode, keyword))
                        .orderByAsc(PracticeType::getSortOrder)
                        .orderByAsc(PracticeType::getId))
                .stream()
                .map(this::toPracticeTypeResponse)
                .toList();
    }

    public PracticeTypeResponse getPracticeType(Long id) {
        return toPracticeTypeResponse(requirePracticeType(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public PracticeTypeResponse createPracticeType(PracticeTypeUpsertRequest request) {
        validatePracticeTypeParent(request.parentId(), null);
        ensurePracticeTypeCodeAvailable(request.typeCode(), null);
        PracticeType practiceType = new PracticeType();
        fillPracticeType(practiceType, request);
        practiceType.setDeleted(0);
        practiceTypeMapper.insert(practiceType);
        return getPracticeType(practiceType.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public PracticeTypeResponse updatePracticeType(Long id, PracticeTypeUpsertRequest request) {
        PracticeType practiceType = requirePracticeType(id);
        validatePracticeTypeParent(request.parentId(), id);
        ensurePracticeTypeCodeAvailable(request.typeCode(), id);
        fillPracticeType(practiceType, request);
        practiceTypeMapper.updateById(practiceType);
        return getPracticeType(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePracticeType(Long id) {
        requirePracticeType(id);
        ensurePracticeTypeDeletable(id);
        practiceTypeMapper.deleteById(id);
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

    private Organization requireOrganization(Long id) {
        Organization organization = organizationMapper.selectById(id);
        if (organization == null || !Objects.equals(organization.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Organization does not exist");
        }
        return organization;
    }

    private PracticeType requirePracticeType(Long id) {
        PracticeType practiceType = practiceTypeMapper.selectById(id);
        if (practiceType == null || !Objects.equals(practiceType.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Practice type does not exist");
        }
        return practiceType;
    }

    private void ensureOrganizationCodeAvailable(String orgCode, Long ignoredId) {
        Organization existing = organizationMapper.selectOne(new LambdaQueryWrapper<Organization>()
                .eq(Organization::getOrgCode, orgCode)
                .eq(Organization::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Organization code already exists");
        }
    }

    private void ensurePracticeTypeCodeAvailable(String typeCode, Long ignoredId) {
        PracticeType existing = practiceTypeMapper.selectOne(new LambdaQueryWrapper<PracticeType>()
                .eq(PracticeType::getTypeCode, typeCode)
                .eq(PracticeType::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Practice type code already exists");
        }
    }

    private void validatePracticeTypeParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(parentId, currentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Practice type parentId must not equal current id");
        }
        requirePracticeType(parentId);
    }

    private void ensureOrganizationUnused(Long id) {
        long studentCount = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getOrganizationId, id)
                .eq(Student::getDeleted, 0));
        if (studentCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Organization is already referenced by students");
        }
        long expertCount = expertMapper.selectCount(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getOrganizationId, id)
                .eq(Expert::getDeleted, 0));
        if (expertCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Organization is already referenced by experts");
        }
    }

    private void ensurePracticeTypeDeletable(Long id) {
        long childCount = practiceTypeMapper.selectCount(new LambdaQueryWrapper<PracticeType>()
                .eq(PracticeType::getParentId, id)
                .eq(PracticeType::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Practice type still has child nodes");
        }
        long studentCount = studentMapper.selectCount(new LambdaQueryWrapper<Student>()
                .eq(Student::getPracticeTypeId, id)
                .eq(Student::getDeleted, 0));
        if (studentCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Practice type is already referenced by students");
        }
        long expertCount = expertMapper.selectCount(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getPracticeTypeId, id)
                .eq(Expert::getDeleted, 0));
        if (expertCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Practice type is already referenced by experts");
        }
    }

    private void fillOrganization(Organization organization, OrganizationUpsertRequest request) {
        organization.setOrgCode(normalizeText(request.orgCode()));
        organization.setOrgName(request.orgName().trim());
        organization.setOrgType(normalizeText(request.orgType()));
        organization.setProvinceCode(normalizeText(request.provinceCode()));
        organization.setCityCode(normalizeText(request.cityCode()));
        organization.setDistrictCode(normalizeText(request.districtCode()));
        organization.setAddress(normalizeText(request.address()));
        organization.setStatus(request.status());
        organization.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private void fillPracticeType(PracticeType practiceType, PracticeTypeUpsertRequest request) {
        practiceType.setParentId(request.parentId());
        practiceType.setTypeCode(request.typeCode().trim());
        practiceType.setTypeName(request.typeName().trim());
        practiceType.setStatus(request.status());
        practiceType.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeText(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
