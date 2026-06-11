package com.gugugaga.jsmedicine.module.user.service;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.service.AppUserAvatarUrlResolver;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentImportFailureResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentImportResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentUpsertRequest;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserUpdateRequest;
import com.gugugaga.jsmedicine.module.user.dto.AppUserManagementRole;
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationReviewRequest;
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationFileResponse;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.entity.StudentCertificationFile;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentCertificationFileMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminUserService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String FILE_EXTENSION_XLS = ".xls";
    private static final String FILE_EXTENSION_XLSX = ".xlsx";
    private static final String HEADER_STUDENT_NO = "学号";
    private static final String HEADER_REAL_NAME = "姓名";
    private static final String HEADER_GENDER = "性别";
    private static final String HEADER_AGE = "年龄";
    private static final String HEADER_EDUCATION_LEVEL = "学历";
    private static final String HEADER_MOBILE = "手机号";
    private static final String HEADER_ID_CARD_NO = "身份证号";
    private static final String HEADER_PROVINCE = "省份";
    private static final String HEADER_PROVINCE_CODE = "省份编码";
    private static final String HEADER_CITY = "城市";
    private static final String HEADER_CITY_CODE = "城市编码";
    private static final String HEADER_DISTRICT = "区县";
    private static final String HEADER_DISTRICT_CODE = "区县编码";
    private static final String HEADER_ORGANIZATION = "单位";
    private static final String HEADER_ORGANIZATION_ID = "机构ID";
    private static final String HEADER_POSITION_TITLE = "职称";
    private static final String HEADER_PRACTICE_TYPE_ID = "执业类型ID";
    private static final String HEADER_STATUS = "状态";
    private static final String HEADER_CERTIFICATION_STATUS = "认证状态";
    private static final String HEADER_CERTIFICATION_SUBMITTED_AT = "认证提交时间";
    private static final String HEADER_CERTIFICATION_REVIEWED_AT = "认证审核时间";
    private static final String HEADER_CERTIFICATION_REVIEWED_BY = "认证审核人ID";
    private static final String HEADER_REJECT_REASON = "驳回原因";
    private static final String HEADER_ENROLLED_AT = "入学时间";
    private static final List<String> STUDENT_IMPORT_HEADERS = List.of(
            HEADER_STUDENT_NO,
            HEADER_REAL_NAME,
            HEADER_GENDER,
            HEADER_AGE,
            HEADER_EDUCATION_LEVEL,
            HEADER_MOBILE,
            HEADER_ID_CARD_NO,
            HEADER_PROVINCE,
            HEADER_PROVINCE_CODE,
            HEADER_CITY,
            HEADER_CITY_CODE,
            HEADER_DISTRICT,
            HEADER_DISTRICT_CODE,
            HEADER_ORGANIZATION,
            HEADER_ORGANIZATION_ID,
            HEADER_POSITION_TITLE,
            HEADER_PRACTICE_TYPE_ID,
            HEADER_STATUS
    );
    private static final List<String> STUDENT_EXPORT_HEADERS = List.of(
            HEADER_STUDENT_NO,
            HEADER_REAL_NAME,
            HEADER_GENDER,
            HEADER_AGE,
            HEADER_EDUCATION_LEVEL,
            HEADER_MOBILE,
            HEADER_ID_CARD_NO,
            HEADER_PROVINCE,
            HEADER_PROVINCE_CODE,
            HEADER_CITY,
            HEADER_CITY_CODE,
            HEADER_DISTRICT,
            HEADER_DISTRICT_CODE,
            HEADER_ORGANIZATION,
            HEADER_ORGANIZATION_ID,
            HEADER_POSITION_TITLE,
            HEADER_PRACTICE_TYPE_ID,
            HEADER_STATUS,
            HEADER_CERTIFICATION_STATUS,
            HEADER_CERTIFICATION_SUBMITTED_AT,
            HEADER_CERTIFICATION_REVIEWED_AT,
            HEADER_CERTIFICATION_REVIEWED_BY,
            HEADER_REJECT_REASON,
            HEADER_ENROLLED_AT
    );

    private final AppUserMapper appUserMapper;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final StudentMapper studentMapper;
    private final StudentCertificationFileMapper studentCertificationFileMapper;
    private final ExpertMapper expertMapper;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final AppUserAvatarUrlResolver appUserAvatarUrlResolver;
    private final Validator validator;

    public AdminUserService(
            AppUserMapper appUserMapper,
            AppUserIdentityMapper appUserIdentityMapper,
            StudentMapper studentMapper,
            StudentCertificationFileMapper studentCertificationFileMapper,
            ExpertMapper expertMapper,
            CurrentAdminAccessor currentAdminAccessor,
            AppUserAvatarUrlResolver appUserAvatarUrlResolver,
            Validator validator
    ) {
        this.appUserMapper = appUserMapper;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.studentMapper = studentMapper;
        this.studentCertificationFileMapper = studentCertificationFileMapper;
        this.expertMapper = expertMapper;
        this.currentAdminAccessor = currentAdminAccessor;
        this.appUserAvatarUrlResolver = appUserAvatarUrlResolver;
        this.validator = validator;
    }

    public PageResponse<AdminUserResponse> pageUsers(AdminUserPageQuery query) {
        Page<AppUser> page = appUserMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getDeleted, 0)
                        .eq(query.status() != null, AppUser::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(AppUser::getUsername, query.keyword())
                                .or()
                                .like(AppUser::getNickname, query.keyword())
                                .or()
                                .like(AppUser::getMobile, query.keyword()))
                        .orderByAsc("registeredAtAsc".equals(query.sort()), AppUser::getRegisteredAt)
                        .orderByDesc(!"registeredAtAsc".equals(query.sort()), AppUser::getRegisteredAt));
        return new PageResponse<>(
                page.getRecords().stream().map(this::toUserResponse).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public AdminUserResponse getUser(Long id) {
        return toUserResponse(requireUser(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, EnabledStatus status) {
        requireUser(id);
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, id)
                .set(AppUser::getStatus, status));
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        AppUser user = requireUser(id);
        user.setNickname(request.nickname());
        user.setProfileSignature(request.profileSignature());
        user.setStatus(request.status());
        appUserMapper.updateById(user);
        applyUserRole(user.getId(), request);
        return getUser(id);
    }

    public PageResponse<AdminStudentResponse> pageStudents(AdminStudentPageQuery query) {
        Page<Student> page = studentMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                buildStudentQuery(query));
        return new PageResponse<>(
                page.getRecords().stream().map(this::toStudentResponse).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public AdminStudentResponse getStudent(Long id) {
        return toStudentResponse(requireStudent(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminStudentResponse createStudent(AdminStudentUpsertRequest request) {
        Student student = createStudentInternal(request);
        return getStudent(student.getId());
    }

    public AdminStudentImportResponse importStudents(MultipartFile file) {
        validateImportFile(file);
        List<List<Object>> rows = readStudentImportRows(file);
        Map<String, Integer> headerIndexes = buildHeaderIndexes(rows);
        List<AdminStudentImportFailureResponse> failures = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;
        for (int index = 1; index < rows.size(); index++) {
            List<Object> row = rows.get(index);
            if (isEmptyRow(row)) {
                continue;
            }
            totalRows++;
            try {
                AdminStudentUpsertRequest request = toImportRequest(headerIndexes, row);
                createStudentInternal(request);
                successCount++;
            } catch (BusinessException exception) {
                failures.add(new AdminStudentImportFailureResponse(
                        index + 1,
                        getCellText(row, headerIndexes.get(HEADER_STUDENT_NO)),
                        getCellText(row, headerIndexes.get(HEADER_REAL_NAME)),
                        exception.getMessage()
                ));
            }
        }
        return new AdminStudentImportResponse(totalRows, successCount, failures.size(), failures);
    }

    public byte[] exportStudents(AdminStudentPageQuery query) {
        List<Student> students = studentMapper.selectList(buildStudentQuery(query));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.writeHeadRow(STUDENT_EXPORT_HEADERS);
            students.forEach(student -> writer.writeRow(toStudentExportRow(student)));
            writer.flush(outputStream, true);
            writer.close();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to export students");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminStudentResponse updateStudent(Long id, AdminStudentUpsertRequest request) {
        Student student = requireStudent(id);
        if (hasText(request.studentNo())) {
            ensureStudentNoAvailable(request.studentNo(), id);
        }
        applyStudent(student, request);
        studentMapper.updateById(student);
        return getStudent(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStudent(Long id) {
        Student student = requireStudent(id);
        clearStudentAssociations(student);
        studentCertificationFileMapper.delete(new LambdaUpdateWrapper<StudentCertificationFile>()
                .eq(StudentCertificationFile::getStudentId, id));
        studentMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteStudents(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "ids must not be empty");
        }
        ids.stream().distinct().forEach(this::deleteStudent);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminStudentResponse reviewCertification(Long id, StudentCertificationReviewRequest request) {
        Student student = requireStudent(id);
        if (request.certificationStatus() != StudentCertificationStatus.APPROVED
                && request.certificationStatus() != StudentCertificationStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Certification review status must be approved or rejected");
        }
        student.setCertificationStatus(request.certificationStatus());
        student.setCertificationReviewedAt(LocalDateTime.now());
        student.setCertificationReviewedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        student.setRejectReason(request.certificationStatus() == StudentCertificationStatus.REJECTED ? request.rejectReason() : null);
        if (request.certificationStatus() == StudentCertificationStatus.APPROVED && student.getEnrolledAt() == null) {
            student.setEnrolledAt(LocalDateTime.now());
        }
        studentMapper.updateById(student);
        return getStudent(id);
    }

    private AppUser requireUser(Long id) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null || !Objects.equals(user.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "App user does not exist");
        }
        return user;
    }

    private Student requireStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null || !Objects.equals(student.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Student does not exist");
        }
        return student;
    }

    private Student createStudentInternal(AdminStudentUpsertRequest request) {
        validateStudentRequest(request);
        if (hasText(request.studentNo())) {
            ensureStudentNoAvailable(request.studentNo(), null);
        }
        Student student = new Student();
        student.setCertificationStatus(StudentCertificationStatus.APPROVED);
        student.setEnrolledAt(LocalDateTime.now());
        applyStudent(student, request);
        studentMapper.insert(student);
        return student;
    }

    private void validateStudentRequest(AdminStudentUpsertRequest request) {
        Set<ConstraintViolation<AdminStudentUpsertRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, violations.iterator().next().getMessage());
        }
    }

    private LambdaQueryWrapper<Student> buildStudentQuery(AdminStudentPageQuery query) {
        return new LambdaQueryWrapper<Student>()
                .eq(Student::getDeleted, 0)
                .eq(query.status() != null, Student::getStatus, query.status())
                .eq(query.certificationStatus() != null, Student::getCertificationStatus, query.certificationStatus())
                .and(hasText(query.keyword()), wrapper -> wrapper
                        .like(Student::getStudentNo, query.keyword())
                        .or()
                        .like(Student::getRealName, query.keyword())
                        .or()
                        .like(Student::getMobile, query.keyword())
                        .or()
                        .like(Student::getOrganization, query.keyword()))
                .orderByAsc("submittedAtAsc".equals(query.sort()), Student::getCertificationSubmittedAt)
                .orderByDesc(!"submittedAtAsc".equals(query.sort()), Student::getCertificationSubmittedAt);
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Import file must not be empty");
        }
        String fileName = file.getOriginalFilename();
        if (!hasText(fileName)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Import file name is required");
        }
        String normalizedFileName = fileName.toLowerCase();
        if (!normalizedFileName.endsWith(FILE_EXTENSION_XLS) && !normalizedFileName.endsWith(FILE_EXTENSION_XLSX)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Import file must be an Excel file");
        }
    }

    private List<List<Object>> readStudentImportRows(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(inputStream);
            List<List<Object>> rows = reader.read();
            if (rows == null || rows.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Import file must contain header row");
            }
            return rows;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Failed to read import file");
        }
    }

    private Map<String, Integer> buildHeaderIndexes(List<List<Object>> rows) {
        List<Object> headerRow = rows.get(0);
        Map<String, Integer> headerIndexes = new HashMap<>();
        for (int index = 0; index < headerRow.size(); index++) {
            String header = normalizeText(cellToText(headerRow.get(index)));
            if (hasText(header)) {
                headerIndexes.put(header, index);
            }
        }
        List<String> missingHeaders = STUDENT_IMPORT_HEADERS.stream()
                .filter(header -> !headerIndexes.containsKey(header))
                .toList();
        if (!missingHeaders.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Import file headers are invalid: " + String.join(", ", missingHeaders));
        }
        return headerIndexes;
    }

    private AdminStudentUpsertRequest toImportRequest(Map<String, Integer> headerIndexes, List<Object> row) {
        AdminStudentUpsertRequest request = new AdminStudentUpsertRequest(
                normalizeText(getCellText(row, headerIndexes.get(HEADER_STUDENT_NO))),
                requireText(getCellText(row, headerIndexes.get(HEADER_REAL_NAME)), HEADER_REAL_NAME),
                parseGender(getCellText(row, headerIndexes.get(HEADER_GENDER))),
                parseRequiredInteger(getCellText(row, headerIndexes.get(HEADER_AGE)), HEADER_AGE),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_EDUCATION_LEVEL))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_MOBILE))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_ID_CARD_NO))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_PROVINCE))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_PROVINCE_CODE))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_CITY))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_CITY_CODE))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_DISTRICT))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_DISTRICT_CODE))),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_ORGANIZATION))),
                parseOptionalLong(getCellText(row, headerIndexes.get(HEADER_ORGANIZATION_ID)), HEADER_ORGANIZATION_ID),
                normalizeText(getCellText(row, headerIndexes.get(HEADER_POSITION_TITLE))),
                parseOptionalLong(getCellText(row, headerIndexes.get(HEADER_PRACTICE_TYPE_ID)), HEADER_PRACTICE_TYPE_ID),
                parseEnabledStatus(getCellText(row, headerIndexes.get(HEADER_STATUS)))
        );
        validateStudentRequest(request);
        return request;
    }

    private List<Object> toStudentExportRow(Student student) {
        return List.of(
                nullToEmpty(student.getStudentNo()),
                nullToEmpty(student.getRealName()),
                genderLabel(student.getGender()),
                student.getAge() == null ? "" : student.getAge(),
                nullToEmpty(student.getEducationLevel()),
                nullToEmpty(student.getMobile()),
                nullToEmpty(student.getIdCardNo()),
                nullToEmpty(student.getProvince()),
                nullToEmpty(student.getProvinceCode()),
                nullToEmpty(student.getCity()),
                nullToEmpty(student.getCityCode()),
                nullToEmpty(student.getDistrict()),
                nullToEmpty(student.getDistrictCode()),
                nullToEmpty(student.getOrganization()),
                student.getOrganizationId() == null ? "" : student.getOrganizationId(),
                nullToEmpty(student.getPositionTitle()),
                student.getPracticeTypeId() == null ? "" : student.getPracticeTypeId(),
                enabledStatusLabel(student.getStatus()),
                certificationStatusLabel(student.getCertificationStatus()),
                formatDateTime(student.getCertificationSubmittedAt()),
                formatDateTime(student.getCertificationReviewedAt()),
                student.getCertificationReviewedBy() == null ? "" : student.getCertificationReviewedBy(),
                nullToEmpty(student.getRejectReason()),
                formatDateTime(student.getEnrolledAt())
        );
    }

    private String getCellText(List<Object> row, Integer index) {
        if (index == null || index < 0 || index >= row.size()) {
            return null;
        }
        return normalizeText(cellToText(row.get(index)));
    }

    private String cellToText(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        }
        return value.toString().trim();
    }

    private boolean isEmptyRow(List<Object> row) {
        return row == null || row.stream().allMatch(cell -> !hasText(cellToText(cell)));
    }

    private String requireText(String value, String headerName) {
        if (!hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, headerName + " must not be blank");
        }
        return value.trim();
    }

    private Gender parseGender(String value) {
        if (!hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, HEADER_GENDER + " must not be blank");
        }
        return switch (value.trim().toUpperCase()) {
            case "0", "UNKNOWN", "未知" -> Gender.UNKNOWN;
            case "1", "MALE", "男" -> Gender.MALE;
            case "2", "FEMALE", "女" -> Gender.FEMALE;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported " + HEADER_GENDER + ": " + value);
        };
    }

    private EnabledStatus parseEnabledStatus(String value) {
        if (!hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, HEADER_STATUS + " must not be blank");
        }
        return switch (value.trim().toUpperCase()) {
            case "0", "DISABLED", "禁用" -> EnabledStatus.DISABLED;
            case "1", "ENABLED", "启用" -> EnabledStatus.ENABLED;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported " + HEADER_STATUS + ": " + value);
        };
    }

    private Integer parseRequiredInteger(String value, String headerName) {
        if (!hasText(value)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, headerName + " must not be blank");
        }
        try {
            return new BigDecimal(value).intValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, headerName + " must be an integer");
        }
    }

    private Long parseOptionalLong(String value, String headerName) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value).longValueExact();
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, headerName + " must be an integer");
        }
    }

    private String genderLabel(Gender gender) {
        if (gender == null) {
            return "";
        }
        return switch (gender) {
            case UNKNOWN -> "未知";
            case MALE -> "男";
            case FEMALE -> "女";
        };
    }

    private String enabledStatusLabel(EnabledStatus status) {
        if (status == null) {
            return "";
        }
        return status == EnabledStatus.ENABLED ? "启用" : "禁用";
    }

    private String certificationStatusLabel(StudentCertificationStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case UNSUBMITTED -> "未提交";
            case PENDING -> "待审核";
            case APPROVED -> "已通过";
            case REJECTED -> "已驳回";
        };
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "" : value.format(EXPORT_DATE_TIME_FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void ensureStudentNoAvailable(String studentNo, Long ignoredId) {
        Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student number already exists");
        }
    }

    private void applyStudent(Student student, AdminStudentUpsertRequest request) {
        student.setStudentNo(normalizeText(request.studentNo()));
        student.setRealName(request.realName().trim());
        student.setGender(request.gender());
        student.setAge(request.age());
        student.setEducationLevel(normalizeText(request.educationLevel()));
        student.setMobile(normalizeText(request.mobile()));
        student.setIdCardNo(normalizeText(request.idCardNo()));
        student.setProvince(normalizeText(request.province()));
        student.setProvinceCode(normalizeText(request.provinceCode()));
        student.setCity(normalizeText(request.city()));
        student.setCityCode(normalizeText(request.cityCode()));
        student.setDistrict(normalizeText(request.district()));
        student.setDistrictCode(normalizeText(request.districtCode()));
        student.setOrganization(normalizeText(request.organization()));
        student.setOrganizationId(request.organizationId());
        student.setPositionTitle(normalizeText(request.positionTitle()));
        student.setPracticeTypeId(request.practiceTypeId());
        student.setStatus(request.status());
    }

    private void clearStudentAssociations(Student student) {
        if (student.getUserId() != null) {
            deactivateIdentity(student.getUserId(), AppUserIdentityType.STUDENT);
            student.setUserId(null);
            studentMapper.updateById(student);
        }
    }

    private void applyUserRole(Long userId, AdminUserUpdateRequest request) {
        if (request.role() == AppUserManagementRole.NORMAL) {
            deactivateIdentity(userId, AppUserIdentityType.STUDENT);
            deactivateIdentity(userId, AppUserIdentityType.EXPERT);
            unbindCurrentStudent(userId);
            unbindCurrentExpert(userId);
            return;
        }
        if (request.role() == AppUserManagementRole.STUDENT) {
            bindStudent(userId, request);
            ensureIdentity(userId, AppUserIdentityType.STUDENT, true);
            deactivateIdentity(userId, AppUserIdentityType.EXPERT);
            unbindCurrentExpert(userId);
            return;
        }
        ensureIdentity(userId, AppUserIdentityType.EXPERT, true);
        ensureExpertProfile(userId);
        deactivateIdentity(userId, AppUserIdentityType.STUDENT);
        unbindCurrentStudent(userId);
    }

    private void bindStudent(Long userId, AdminUserUpdateRequest request) {
        if (request.studentId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "studentId is required when role is STUDENT");
        }
        Student student = requireStudent(request.studentId());
        if (student.getUserId() != null && !Objects.equals(student.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student is already bound to another user");
        }
        Student currentStudent = findStudentByUserId(userId);
        if (currentStudent != null && !Objects.equals(currentStudent.getId(), student.getId())) {
            currentStudent.setUserId(null);
            studentMapper.updateById(currentStudent);
        }
        student.setUserId(userId);
        student.setProvince(request.province());
        student.setProvinceCode(request.provinceCode());
        student.setCity(request.city());
        student.setCityCode(request.cityCode());
        student.setDistrict(request.district());
        student.setDistrictCode(request.districtCode());
        student.setOrganization(request.organization());
        student.setOrganizationId(request.organizationId());
        student.setPracticeTypeId(request.practiceTypeId());
        studentMapper.updateById(student);
    }

    private void unbindCurrentStudent(Long userId) {
        Student student = findStudentByUserId(userId);
        if (student == null) {
            return;
        }
        student.setUserId(null);
        studentMapper.updateById(student);
    }

    private Student findStudentByUserId(Long userId) {
        return studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private void unbindCurrentExpert(Long userId) {
        Expert expert = findExpertByUserId(userId);
        if (expert == null) {
            return;
        }
        expert.setUserId(null);
        expertMapper.updateById(expert);
    }

    private void ensureExpertProfile(Long userId) {
        Expert expert = findExpertByUserId(userId);
        if (expert != null) {
            return;
        }
        AppUser user = requireUser(userId);
        expert = new Expert();
        expert.setUserId(userId);
        expert.setRealName(resolveExpertRealName(userId, user));
        expert.setGender(user.getGender());
        expert.setMobile(user.getMobile());
        expert.setAvatarUrl(user.getAvatarUrl());
        expert.setOrganization(resolveExpertOrganization(userId));
        expert.setOrganizationId(resolveExpertOrganizationId(userId));
        expert.setPracticeTypeId(resolveExpertPracticeTypeId(userId));
        expert.setStatus(EnabledStatus.ENABLED);
        expert.setConsultEnabled(EnabledStatus.DISABLED);
        expert.setConsultationNotice(null);
        expert.setSortOrder(0);
        expert.setDeleted(0);
        expertMapper.insert(expert);
    }

    private Expert findExpertByUserId(Long userId) {
        return expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .eq(Expert::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private String resolveExpertRealName(Long userId, AppUser user) {
        Student student = findStudentByUserId(userId);
        if (student != null && hasText(student.getRealName())) {
            return student.getRealName();
        }
        if (hasText(user.getNickname())) {
            return user.getNickname();
        }
        if (hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "Expert-" + userId;
    }

    private String resolveExpertOrganization(Long userId) {
        Student student = findStudentByUserId(userId);
        return student == null ? null : student.getOrganization();
    }

    private Long resolveExpertOrganizationId(Long userId) {
        Student student = findStudentByUserId(userId);
        return student == null ? null : student.getOrganizationId();
    }

    private Long resolveExpertPracticeTypeId(Long userId) {
        Student student = findStudentByUserId(userId);
        return student == null ? null : student.getPracticeTypeId();
    }

    private void ensureIdentity(Long userId, AppUserIdentityType identityType, boolean primary) {
        AppUserIdentity identity = findIdentity(userId, identityType);
        if (identity == null) {
            identity = new AppUserIdentity();
            identity.setUserId(userId);
            identity.setIdentityType(identityType);
            identity.setDeleted(0);
        }
        identity.setIdentityStatus(AppUserIdentityStatus.ACTIVE);
        identity.setIsPrimary(primary);
        if (identity.getActivatedAt() == null) {
            identity.setActivatedAt(LocalDateTime.now());
        }
        identity.setDeactivatedAt(null);
        if (identity.getId() == null) {
            appUserIdentityMapper.insert(identity);
        } else {
            appUserIdentityMapper.updateById(identity);
        }
    }

    private void deactivateIdentity(Long userId, AppUserIdentityType identityType) {
        AppUserIdentity identity = findIdentity(userId, identityType);
        if (identity == null || identity.getIdentityStatus() == AppUserIdentityStatus.INACTIVE) {
            return;
        }
        identity.setIdentityStatus(AppUserIdentityStatus.INACTIVE);
        identity.setIsPrimary(false);
        identity.setDeactivatedAt(LocalDateTime.now());
        appUserIdentityMapper.updateById(identity);
    }

    private AppUserIdentity findIdentity(Long userId, AppUserIdentityType identityType) {
        return appUserIdentityMapper.selectOne(new LambdaQueryWrapper<AppUserIdentity>()
                .eq(AppUserIdentity::getUserId, userId)
                .eq(AppUserIdentity::getIdentityType, identityType)
                .eq(AppUserIdentity::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private AppUserManagementRole resolveRole(Long userId) {
        AppUserIdentity expertIdentity = findIdentity(userId, AppUserIdentityType.EXPERT);
        if (expertIdentity != null && expertIdentity.getIdentityStatus() == AppUserIdentityStatus.ACTIVE) {
            return AppUserManagementRole.EXPERT;
        }
        AppUserIdentity studentIdentity = findIdentity(userId, AppUserIdentityType.STUDENT);
        if (studentIdentity != null && studentIdentity.getIdentityStatus() == AppUserIdentityStatus.ACTIVE) {
            return AppUserManagementRole.STUDENT;
        }
        return AppUserManagementRole.NORMAL;
    }

    private AdminUserResponse toUserResponse(AppUser user) {
        Student student = findStudentByUserId(user.getId());
        Expert expert = findExpertByUserId(user.getId());
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileSignature(),
                appUserAvatarUrlResolver.resolve(user.getId(), user.getAvatarUrl()),
                user.getAuthProvider(),
                user.getWechatOpenId(),
                user.getWechatUnionId(),
                user.getGender(),
                user.getStatus(),
                user.getRegisteredAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getProfileCompleted(),
                resolveRole(user.getId()),
                expert == null ? null : expert.getId(),
                expert == null ? null : expert.getRealName(),
                expert != null,
                student == null ? null : student.getId(),
                student == null ? null : student.getRealName(),
                student == null ? null : student.getProvince(),
                student == null ? null : student.getProvinceCode(),
                student == null ? null : student.getCity(),
                student == null ? null : student.getCityCode(),
                student == null ? null : student.getDistrict(),
                student == null ? null : student.getDistrictCode(),
                student == null ? null : student.getOrganization(),
                student == null ? null : student.getOrganizationId(),
                student == null ? null : student.getPracticeTypeId()
        );
    }

    private AdminStudentResponse toStudentResponse(Student student) {
        return new AdminStudentResponse(
                student.getId(),
                student.getUserId(),
                student.getStudentNo(),
                student.getRealName(),
                student.getGender(),
                student.getAge(),
                student.getEducationLevel(),
                student.getMobile(),
                student.getIdCardNo(),
                student.getProvince(),
                student.getProvinceCode(),
                student.getCity(),
                student.getCityCode(),
                student.getDistrict(),
                student.getDistrictCode(),
                student.getOrganization(),
                student.getOrganizationId(),
                student.getPositionTitle(),
                student.getPracticeTypeId(),
                student.getStatus(),
                student.getCertificationStatus(),
                student.getCertificationSubmittedAt(),
                student.getCertificationReviewedAt(),
                student.getCertificationReviewedBy(),
                student.getRejectReason(),
                student.getCertificationMaterials(),
                loadCertificationFiles(student.getId()),
                student.getEnrolledAt()
        );
    }

    private List<StudentCertificationFileResponse> loadCertificationFiles(Long studentId) {
        return studentCertificationFileMapper.selectList(new LambdaQueryWrapper<StudentCertificationFile>()
                        .eq(StudentCertificationFile::getStudentId, studentId))
                .stream()
                .sorted(Comparator.comparing(StudentCertificationFile::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(StudentCertificationFile::getId))
                .map(file -> new StudentCertificationFileResponse(
                        file.getId(),
                        file.getFileAssetId(),
                        file.getSourceUrl(),
                        file.getMaterialType(),
                        file.getSortOrder()
                ))
                .toList();
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeText(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
