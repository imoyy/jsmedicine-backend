package com.gugugaga.jsmedicine.module.statistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.content.topic.entity.Topic;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicMapper;
import com.gugugaga.jsmedicine.module.statistics.dto.ContentInteractionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamPaperScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamScoreSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.RegionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StatisticsQuery;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentScoreUpdateRequest;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursRegionResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursResourceResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.mapper.AdminStatisticsMapper;
import com.gugugaga.jsmedicine.module.statistics.entity.StudentScoreRecord;
import com.gugugaga.jsmedicine.module.statistics.mapper.StudentScoreRecordMapper;
import com.gugugaga.jsmedicine.module.statistics.dto.TopicStudentStatisticsSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.TopicStudentStatisticsPageResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.TopicStudentStatisticsRecordResponse;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class AdminStatisticsService {
    private static final Set<String> SUPPORTED_REGION_DIMENSIONS = Set.of("province", "city", "district");
    private static final Set<String> SUPPORTED_TOPIC_LEARNING_STATUSES = Set.of("not_started", "learning", "completed");

    private final AdminStatisticsMapper statisticsMapper;
    private final StudentScoreRecordMapper studentScoreRecordMapper;
    private final StudentMapper studentMapper;
    private final TopicMapper topicMapper;

    public AdminStatisticsService(
            AdminStatisticsMapper statisticsMapper,
            StudentScoreRecordMapper studentScoreRecordMapper,
            StudentMapper studentMapper,
            TopicMapper topicMapper
    ) {
        this.statisticsMapper = statisticsMapper;
        this.studentScoreRecordMapper = studentScoreRecordMapper;
        this.studentMapper = studentMapper;
        this.topicMapper = topicMapper;
    }

    public StudyHoursSummaryResponse studyHoursSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudyHoursSummary(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city(), normalized.district());
    }

    public List<StudyHoursResourceResponse> studyHoursByResource(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudyHoursByResource(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.studentId(),
                normalized.province(), normalized.city(), normalized.district());
    }

    public List<StudyHoursRegionResponse> studyHoursByRegion(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudyHoursByRegion(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city(), normalized.district(), normalized.dimension());
    }

    public StudentSummaryResponse studentSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalizeStudentPopulationQuery(query);
        return statisticsMapper.selectStudentSummary(
                normalized.startAt(), normalized.endAt(), normalized.province(), normalized.city(), normalized.district());
    }

    public List<RegionStatisticsResponse> regionStatistics(StatisticsQuery query) {
        StatisticsQuery normalized = normalizeStudentPopulationQuery(query);
        return statisticsMapper.selectRegionStatistics(
                normalized.startAt(), normalized.endAt(), normalized.province(), normalized.city(), normalized.district());
    }

    public TopicStudentStatisticsPageResponse topicStudentStatistics(
            Long topicId,
            long page,
            long size,
            String keyword,
            String learningStatus,
            StatisticsQuery query
    ) {
        requireTopic(topicId);
        StatisticsQuery normalized = normalize(query);
        long normalizedPage = normalizePage(page);
        long normalizedSize = normalizeSize(size);
        String normalizedKeyword = blankToNull(keyword);
        String normalizedLearningStatus = normalizeTopicLearningStatus(learningStatus);
        TopicStudentStatisticsSummaryResponse summary = statisticsMapper.selectTopicStudentStatisticsSummary(
                topicId, normalized.startAt(), normalized.endAt(), normalizedKeyword,
                normalized.province(), normalized.city(), normalized.district());
        long total = statisticsMapper.countTopicStudentStatisticsRecords(
                topicId, normalized.startAt(), normalized.endAt(), normalizedKeyword,
                normalized.province(), normalized.city(), normalized.district(), normalizedLearningStatus);
        List<TopicStudentStatisticsRecordResponse> records =
                statisticsMapper.selectTopicStudentStatisticsRecords(
                        topicId, normalized.startAt(), normalized.endAt(), normalizedKeyword,
                        normalized.province(), normalized.city(), normalized.district(), normalizedLearningStatus,
                        (normalizedPage - 1) * normalizedSize, normalizedSize);
        return new TopicStudentStatisticsPageResponse(summary, records, total, normalizedPage, normalizedSize);
    }

    public ExamScoreSummaryResponse examScoreSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectExamScoreSummary(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city(), normalized.district());
    }

    public List<ExamPaperScoreResponse> examScoresByPaper(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectExamScoresByPaper(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city(), normalized.district());
    }

    public PageResponse<StudentScoreResponse> pageStudentScores(long page, long size, String keyword) {
        long normalizedPage = normalizePage(page);
        long normalizedSize = normalizeSize(size);
        String normalizedKeyword = blankToNull(keyword);
        long total = statisticsMapper.countStudentScores(normalizedKeyword);
        List<StudentScoreResponse> records = statisticsMapper.selectStudentScores(
                normalizedKeyword, (normalizedPage - 1) * normalizedSize, normalizedSize);
        return new PageResponse<>(records, total, normalizedPage, normalizedSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public StudentScoreResponse updateStudentScore(Long studentId, StudentScoreUpdateRequest request) {
        requireStudent(studentId);
        StudentScoreRecord scoreRecord = studentScoreRecordMapper.selectOne(new LambdaQueryWrapper<StudentScoreRecord>()
                .eq(StudentScoreRecord::getStudentId, studentId)
                .eq(StudentScoreRecord::getDeleted, 0)
                .last("LIMIT 1"));
        if (scoreRecord == null) {
            scoreRecord = new StudentScoreRecord();
            scoreRecord.setStudentId(studentId);
            applyStudentScoreUpdate(scoreRecord, request);
            studentScoreRecordMapper.insert(scoreRecord);
        } else {
            applyStudentScoreUpdate(scoreRecord, request);
            studentScoreRecordMapper.updateById(scoreRecord);
        }
        List<StudentScoreResponse> records = statisticsMapper.selectStudentScoresByStudentId(studentId);
        if (records.isEmpty()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "student score update result not found");
        }
        return records.get(0);
    }

    public List<ContentInteractionStatisticsResponse> contentInteractions(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectContentInteractionStatistics(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId());
    }

    private StatisticsQuery normalize(StatisticsQuery query) {
        LocalDateTime endAt = query.endAt() == null ? LocalDateTime.now() : query.endAt();
        LocalDateTime startAt = query.startAt() == null ? endAt.minusDays(30) : query.startAt();
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startAt must be before endAt");
        }
        return new StatisticsQuery(
                startAt,
                endAt,
                blankToNull(query.resourceType()),
                query.resourceId(),
                query.studentId(),
                blankToNull(query.province()),
                blankToNull(query.city()),
                blankToNull(query.district()),
                normalizeDimension(query.dimension())
        );
    }

    private StatisticsQuery normalizeStudentPopulationQuery(StatisticsQuery query) {
        LocalDateTime startAt = query.startAt();
        LocalDateTime endAt = query.endAt();
        if (startAt != null && endAt != null && !startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startAt must be before endAt");
        }
        return new StatisticsQuery(
                startAt,
                endAt,
                blankToNull(query.resourceType()),
                query.resourceId(),
                query.studentId(),
                blankToNull(query.province()),
                blankToNull(query.city()),
                blankToNull(query.district()),
                normalizeDimension(query.dimension())
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeDimension(String dimension) {
        String normalized = blankToNull(dimension);
        if (normalized == null) {
            return "city";
        }
        if (!SUPPORTED_REGION_DIMENSIONS.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "dimension must be one of province, city or district");
        }
        return normalized;
    }

    private String normalizeTopicLearningStatus(String learningStatus) {
        String normalized = blankToNull(learningStatus);
        if (normalized == null) {
            return null;
        }
        if (!SUPPORTED_TOPIC_LEARNING_STATUSES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "learningStatus must be one of not_started, learning or completed");
        }
        return normalized;
    }

    private long normalizePage(long page) {
        return Math.max(page, 1);
    }

    private long normalizeSize(long size) {
        return size <= 0 ? 20 : Math.min(size, 100);
    }

    private void applyStudentScoreUpdate(StudentScoreRecord scoreRecord, StudentScoreUpdateRequest request) {
        scoreRecord.setTheoryTrainingStatus(request.theoryTrainingStatus());
        scoreRecord.setClinicalPracticeStatus(request.clinicalPracticeStatus());
        scoreRecord.setPracticalAssessmentStatus(request.practicalAssessmentStatus());
        scoreRecord.setTheoryAssessmentStatus(request.theoryAssessmentStatus());
        scoreRecord.setOnlineTrainingStatus(request.onlineTrainingStatus());
    }

    private void requireStudent(Long studentId) {
        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getId, studentId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "student not found");
        }
    }

    private void requireTopic(Long topicId) {
        Topic topic = topicMapper.selectOne(new LambdaQueryWrapper<Topic>()
                .eq(Topic::getId, topicId)
                .eq(Topic::getDeleted, 0)
                .last("LIMIT 1"));
        if (topic == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "topic not found");
        }
    }
}
