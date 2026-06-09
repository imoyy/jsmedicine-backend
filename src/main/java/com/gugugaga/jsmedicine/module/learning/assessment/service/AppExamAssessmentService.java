package com.gugugaga.jsmedicine.module.learning.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.AssessmentEventType;
import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.enums.ExamSubmitType;
import com.gugugaga.jsmedicine.common.enums.QuestionType;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAnswerResultResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAssessmentEnterRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAssessmentResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamQuestionOptionResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamSubmitRequest;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessment;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessmentStudent;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentStudentMapper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaperQuestion;
import com.gugugaga.jsmedicine.module.learning.question.entity.Question;
import com.gugugaga.jsmedicine.module.learning.question.entity.QuestionOption;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperQuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionOptionMapper;
import com.gugugaga.jsmedicine.module.learning.record.entity.ExamRecord;
import com.gugugaga.jsmedicine.module.learning.record.entity.ExamRecordAnswer;
import com.gugugaga.jsmedicine.module.learning.record.mapper.ExamRecordAnswerMapper;
import com.gugugaga.jsmedicine.module.learning.record.mapper.ExamRecordMapper;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppExamAssessmentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final ExamAssessmentStudentMapper examAssessmentStudentMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamRecordAnswerMapper examRecordAnswerMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final StudentMapper studentMapper;
    private final ExamAssessmentSupportService assessmentSupportService;
    private final CurrentAppUserResolver currentAppUserResolver;

    public AppExamAssessmentService(
            ExamAssessmentStudentMapper examAssessmentStudentMapper,
            ExamRecordMapper examRecordMapper,
            ExamRecordAnswerMapper examRecordAnswerMapper,
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            QuestionMapper questionMapper,
            QuestionOptionMapper questionOptionMapper,
            StudentMapper studentMapper,
            ExamAssessmentSupportService assessmentSupportService,
            CurrentAppUserResolver currentAppUserResolver
    ) {
        this.examAssessmentStudentMapper = examAssessmentStudentMapper;
        this.examRecordMapper = examRecordMapper;
        this.examRecordAnswerMapper = examRecordAnswerMapper;
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.studentMapper = studentMapper;
        this.assessmentSupportService = assessmentSupportService;
        this.currentAppUserResolver = currentAppUserResolver;
    }

    public PageResponse<AppExamAssessmentResponse> pageAssessments(long page, long size) {
        Student student = requireCurrentStudent();
        List<ExamAssessmentStudent> snapshots = examAssessmentStudentMapper.selectList(new LambdaQueryWrapper<ExamAssessmentStudent>()
                .eq(ExamAssessmentStudent::getStudentId, student.getId())
                .orderByDesc(ExamAssessmentStudent::getCreatedAt));
        List<AppExamAssessmentResponse> responses = snapshots.stream()
                .map(snapshot -> toAssessmentResponse(snapshot.getAssessmentId(), student.getId()))
                .distinct()
                .sorted(Comparator.comparing(AppExamAssessmentResponse::startAt).reversed())
                .toList();
        long normalizedPage = normalizePage(page);
        long normalizedSize = normalizeSize(size);
        int fromIndex = (int) Math.min(responses.size(), Math.max(0, (normalizedPage - 1) * normalizedSize));
        int toIndex = (int) Math.min(responses.size(), fromIndex + normalizedSize);
        return new PageResponse<>(responses.subList(fromIndex, toIndex), responses.size(), normalizedPage, normalizedSize);
    }

    public AppExamAssessmentResponse getAssessment(Long assessmentId) {
        Student student = requireCurrentStudent();
        requireAssignment(assessmentId, student.getId());
        return toAssessmentResponse(assessmentId, student.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppExamRecordResponse enterAssessment(Long assessmentId, AppExamAssessmentEnterRequest request) {
        Student student = requireCurrentStudent();
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        ExamAssessmentStudent snapshot = requireAssignment(assessmentId, student.getId());
        AssessmentStatus displayStatus = assessmentSupportService.resolveDisplayStatus(assessment, LocalDateTime.now());
        if (displayStatus != AssessmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT, "Exam assessment is not in progress");
        }
        String requestId = assessmentSupportService.normalizeText(request.requestId());
        ExamRecord record = findAssessmentRecord(assessmentId, student.getId()).orElse(null);
        if (record != null && assessmentSupportService.isTerminalRecord(record)) {
            return toExamRecordResponse(record, true);
        }
        if (record != null && Objects.equals(record.getLastEnterRequestId(), requestId) && requestId != null) {
            return toExamRecordResponse(record, true);
        }
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new ExamRecord();
            record.setStudentId(student.getId());
            record.setPaperId(assessment.getPaperId());
            record.setAssessmentId(assessmentId);
            record.setScore(BigDecimal.ZERO.setScale(2));
            record.setPassed(0);
            record.setStatus(ExamRecordStatus.IN_PROGRESS);
            record.setStartedAt(now);
            record.setLastActiveAt(now);
            record.setLastEnterRequestId(requestId);
            examRecordMapper.insert(record);
            assessmentSupportService.saveEventIfNecessary(snapshot, AssessmentEventType.ENTER, requestId, "Enter assessment");
            return toExamRecordResponse(record, true);
        }
        record.setLastActiveAt(now);
        record.setLastEnterRequestId(requestId);
        examRecordMapper.updateById(record);
        return toExamRecordResponse(record, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppExamRecordResponse submitAssessment(Long assessmentId, AppExamSubmitRequest request) {
        Student student = requireCurrentStudent();
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        ExamAssessmentStudent snapshot = requireAssignment(assessmentId, student.getId());
        ExamRecord record = findAssessmentRecord(assessmentId, student.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFLICT, "Assessment has not been entered"));
        String requestId = assessmentSupportService.normalizeText(request.requestId());
        if (assessmentSupportService.isTerminalRecord(record)) {
            return toExamRecordResponse(record, true);
        }
        if (requestId != null && Objects.equals(record.getLastSubmitRequestId(), requestId)) {
            return toExamRecordResponse(record, true);
        }
        if (assessmentSupportService.resolveDisplayStatus(assessment, LocalDateTime.now()) != AssessmentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.CONFLICT, "Exam assessment is not in progress");
        }
        ExamPaper paper = assessmentSupportService.requireExamPaper(assessment.getPaperId());
        List<ExamPaperQuestion> paperQuestions = loadPaperQuestionRelations(paper.getId());
        if (paperQuestions.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Exam paper has no questions");
        }
        Map<Long, String> submittedAnswers = request.answers().stream()
                .collect(Collectors.toMap(
                        AppExamSubmitRequest.Answer::questionId,
                        answer -> normalizeAnswer(answer.answerContent()),
                        (left, right) -> right,
                        LinkedHashMap::new));
        BigDecimal totalScore = BigDecimal.ZERO.setScale(2);
        for (ExamPaperQuestion relation : paperQuestions) {
            Question question = requireVisibleQuestion(relation.getQuestionId());
            String answerContent = submittedAnswers.getOrDefault(question.getId(), "");
            BigDecimal answerScore = gradeAnswer(question, relation.getScore(), answerContent);
            totalScore = totalScore.add(answerScore);
            ExamRecordAnswer answer = new ExamRecordAnswer();
            answer.setExamRecordId(record.getId());
            answer.setQuestionId(question.getId());
            answer.setAnswerContent(answerContent);
            answer.setScore(answerScore);
            answer.setCorrect(answerScore.compareTo(BigDecimal.ZERO) > 0 && answerScore.compareTo(relation.getScore()) == 0 ? 1 : 0);
            examRecordAnswerMapper.insert(answer);
        }
        LocalDateTime now = LocalDateTime.now();
        record.setScore(totalScore.setScale(2, RoundingMode.HALF_UP));
        record.setPassed(record.getScore().compareTo(paper.getPassScore()) >= 0 ? 1 : 0);
        record.setStatus(ExamRecordStatus.SUBMITTED);
        record.setSubmitType(ExamSubmitType.NORMAL);
        record.setSubmittedAt(now);
        record.setLastActiveAt(now);
        record.setLastSubmitRequestId(requestId);
        examRecordMapper.updateById(record);
        assessmentSupportService.saveEventIfNecessary(snapshot, AssessmentEventType.SUBMIT, requestId, "Submit assessment");
        return toExamRecordResponse(record, true);
    }

    private AppExamAssessmentResponse toAssessmentResponse(Long assessmentId, Long studentId) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        ExamPaper paper = assessmentSupportService.requireExamPaper(assessment.getPaperId());
        LocalDateTime now = LocalDateTime.now();
        AssessmentStatus displayStatus = assessmentSupportService.resolveDisplayStatus(assessment, now);
        ExamRecord record = findAssessmentRecord(assessmentId, studentId).orElse(null);
        return new AppExamAssessmentResponse(
                assessment.getId(),
                assessment.getAssessmentName(),
                assessment.getPaperId(),
                paper.getPaperName(),
                assessment.getAssessmentType(),
                displayStatus,
                assessment.getStartAt(),
                assessment.getEndAt(),
                now,
                assessmentSupportService.countdownSeconds(displayStatus, assessment, now),
                paper.getDurationMinutes(),
                record == null ? null : record.getId(),
                record == null ? null : record.getStatus()
        );
    }

    private AppExamRecordResponse toExamRecordResponse(ExamRecord record, boolean includeAnswers) {
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        return new AppExamRecordResponse(
                record.getId(),
                record.getStudentId(),
                record.getPaperId(),
                record.getAssessmentId(),
                paper == null ? null : paper.getPaperName(),
                record.getSourceType(),
                record.getSourceId(),
                record.getScore(),
                record.getPassed(),
                record.getStatus(),
                record.getSubmitType(),
                record.getStartedAt(),
                record.getSubmittedAt(),
                record.getLastActiveAt(),
                includeAnswers ? loadExamAnswerResults(record.getId()) : List.of()
        );
    }

    private List<AppExamAnswerResultResponse> loadExamAnswerResults(Long recordId) {
        return examRecordAnswerMapper.selectList(new LambdaQueryWrapper<ExamRecordAnswer>()
                        .eq(ExamRecordAnswer::getExamRecordId, recordId)
                        .orderByAsc(ExamRecordAnswer::getId))
                .stream()
                .map(this::toExamAnswerResultResponse)
                .toList();
    }

    private AppExamAnswerResultResponse toExamAnswerResultResponse(ExamRecordAnswer answer) {
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            return new AppExamAnswerResultResponse(answer.getQuestionId(), null, null, answer.getAnswerContent(),
                    null, null, answer.getScore(), answer.getCorrect(), List.of());
        }
        return new AppExamAnswerResultResponse(
                question.getId(),
                question.getQuestionType(),
                question.getTitle(),
                answer.getAnswerContent(),
                correctAnswer(question.getId()),
                question.getAnalysis(),
                answer.getScore(),
                answer.getCorrect(),
                loadExamQuestionOptions(question.getId())
        );
    }

    private List<AppExamQuestionOptionResponse> loadExamQuestionOptions(Long questionId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, questionId)
                        .orderByAsc(QuestionOption::getSortOrder)
                        .orderByAsc(QuestionOption::getOptionKey))
                .stream()
                .map(option -> new AppExamQuestionOptionResponse(option.getId(), option.getOptionKey(), option.getOptionContent(), option.getSortOrder()))
                .toList();
    }

    private List<ExamPaperQuestion> loadPaperQuestionRelations(Long paperId) {
        return examPaperQuestionMapper.selectList(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paperId)
                .orderByAsc(ExamPaperQuestion::getSortOrder)
                .orderByAsc(ExamPaperQuestion::getId));
    }

    private Question requireVisibleQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || !Objects.equals(question.getDeleted(), 0) || question.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private BigDecimal gradeAnswer(Question question, BigDecimal score, String answerContent) {
        if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
            return BigDecimal.ZERO.setScale(2);
        }
        Set<String> submitted = answerSet(answerContent);
        Set<String> correct = correctAnswerSet(question.getId());
        if (!correct.isEmpty() && submitted.equals(correct)) {
            return score.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }

    private String correctAnswer(Long questionId) {
        return correctAnswerSet(questionId).stream().sorted().collect(Collectors.joining(","));
    }

    private Set<String> correctAnswerSet(Long questionId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, questionId)
                        .eq(QuestionOption::getCorrect, 1))
                .stream()
                .map(QuestionOption::getOptionKey)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> answerSet(String answerContent) {
        return Arrays.stream(normalizeAnswer(answerContent).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeAnswer(String answerContent) {
        if (answerContent == null) {
            return "";
        }
        return Arrays.stream(answerContent.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .sorted()
                .collect(Collectors.joining(","));
    }

    private Student requireCurrentStudent() {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        Student student = findStudentByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "Student certification is required"));
        if (student.getStatus() != EnabledStatus.ENABLED || student.getCertificationStatus() != StudentCertificationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Student certification is not approved");
        }
        return student;
    }

    private Optional<Student> findStudentByUserId(Long userId) {
        return Optional.ofNullable(studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private ExamAssessmentStudent requireAssignment(Long assessmentId, Long studentId) {
        ExamAssessmentStudent snapshot = examAssessmentStudentMapper.selectOne(new LambdaQueryWrapper<ExamAssessmentStudent>()
                .eq(ExamAssessmentStudent::getAssessmentId, assessmentId)
                .eq(ExamAssessmentStudent::getStudentId, studentId)
                .last("LIMIT 1"));
        if (snapshot == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Current student is not assigned to this assessment");
        }
        return snapshot;
    }

    private Optional<ExamRecord> findAssessmentRecord(Long assessmentId, Long studentId) {
        return Optional.ofNullable(examRecordMapper.selectOne(new LambdaQueryWrapper<ExamRecord>()
                .eq(ExamRecord::getAssessmentId, assessmentId)
                .eq(ExamRecord::getStudentId, studentId)
                .last("LIMIT 1")));
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
}
