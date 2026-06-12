package com.gugugaga.jsmedicine.module.interaction.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertResponse;
import com.gugugaga.jsmedicine.module.expert.admin.service.AdminExpertService;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.service.AppUserAvatarUrlResolver;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackProcessRequest;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerRequest;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.feedback.entity.Feedback;
import com.gugugaga.jsmedicine.module.interaction.feedback.mapper.FeedbackMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaAnswer;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaQuestion;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaAnswerMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaQuestionMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminInteractionService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final FeedbackMapper feedbackMapper;
    private final AppUserMapper appUserMapper;
    private final AdminExpertService adminExpertService;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final AuditRecordService auditRecordService;
    private final AppUserAvatarUrlResolver appUserAvatarUrlResolver;

    public AdminInteractionService(
            QaQuestionMapper qaQuestionMapper,
            QaAnswerMapper qaAnswerMapper,
            FeedbackMapper feedbackMapper,
            AppUserMapper appUserMapper,
            AdminExpertService adminExpertService,
            CurrentAdminAccessor currentAdminAccessor,
            AuditRecordService auditRecordService,
            AppUserAvatarUrlResolver appUserAvatarUrlResolver
    ) {
        this.qaQuestionMapper = qaQuestionMapper;
        this.qaAnswerMapper = qaAnswerMapper;
        this.feedbackMapper = feedbackMapper;
        this.appUserMapper = appUserMapper;
        this.adminExpertService = adminExpertService;
        this.currentAdminAccessor = currentAdminAccessor;
        this.auditRecordService = auditRecordService;
        this.appUserAvatarUrlResolver = appUserAvatarUrlResolver;
    }

    public PageResponse<QaQuestionResponse> pageQaQuestions(long page, long size, String keyword, QaStatus status) {
        Page<QaQuestion> questionPage = qaQuestionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<QaQuestion>()
                        .eq(QaQuestion::getDeleted, 0)
                        .eq(status != null, QaQuestion::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper.like(QaQuestion::getTitle, keyword).or().like(QaQuestion::getContent, keyword))
                        .orderByDesc(QaQuestion::getCreatedAt));
        return pageResponse(questionPage, questionPage.getRecords().stream().map(question -> toQaQuestionResponse(question, false)).toList());
    }

    public QaQuestionResponse qaQuestionDetail(Long id) {
        return toQaQuestionResponse(requireQaQuestion(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertResponse createConsultExpert(ExpertRequest request) {
        if (request.consultEnabled() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Consult expert creation requires consultEnabled to be ENABLED");
        }
        return adminExpertService.createExpert(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public QaQuestionResponse answerQuestion(Long questionId, QaAnswerRequest request) {
        QaQuestion question = requireQaQuestion(questionId);
        QaStatus before = question.getStatus() == null ? QaStatus.PENDING : question.getStatus();
        QaAnswer answer = new QaAnswer();
        answer.setQuestionId(questionId);
        answer.setAdminId(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        answer.setExpertId(request.expertId());
        answer.setContent(request.content());
        answer.setAnsweredAt(LocalDateTime.now());
        answer.setDeleted(0);
        qaAnswerMapper.insert(answer);
        question.setStatus(QaStatus.ANSWERED);
        qaQuestionMapper.updateById(question);
        saveAudit("qa_question", questionId, before.getValue(), QaStatus.ANSWERED.getValue(), request.auditComment());
        return toQaQuestionResponse(question, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQaQuestion(Long id) {
        requireQaQuestion(id);
        qaQuestionMapper.deleteById(id);
    }

    public PageResponse<FeedbackResponse> pageFeedbacks(long page, long size, String keyword, FeedbackStatus status) {
        Page<Feedback> feedbackPage = feedbackMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getDeleted, 0)
                        .eq(status != null, Feedback::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper.like(Feedback::getContent, keyword).or().like(Feedback::getContact, keyword))
                        .orderByDesc(Feedback::getCreatedAt));
        return pageResponse(feedbackPage, feedbackPage.getRecords().stream().map(this::toFeedbackResponse).toList());
    }

    public FeedbackResponse feedbackDetail(Long id) {
        return toFeedbackResponse(requireFeedback(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedbackResponse processFeedback(Long id, FeedbackProcessRequest request) {
        Feedback feedback = requireFeedback(id);
        FeedbackStatus before = feedback.getStatus();
        feedback.setStatus(FeedbackStatus.PROCESSED);
        feedback.setProcessedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        feedback.setProcessedAt(LocalDateTime.now());
        feedback.setProcessNote(request.processNote());
        feedbackMapper.updateById(feedback);
        saveAudit("feedback", id, before.getValue(), FeedbackStatus.PROCESSED.getValue(), request.processNote());
        return toFeedbackResponse(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedback(Long id) {
        requireFeedback(id);
        feedbackMapper.deleteById(id);
    }

    private QaQuestion requireQaQuestion(Long id) {
        QaQuestion question = qaQuestionMapper.selectById(id);
        if (question == null || !Objects.equals(question.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "QA question does not exist");
        }
        return question;
    }

    private Feedback requireFeedback(Long id) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null || !Objects.equals(feedback.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Feedback does not exist");
        }
        return feedback;
    }

    private QaQuestionResponse toQaQuestionResponse(QaQuestion question, boolean includeAnswers) {
        return new QaQuestionResponse(question.getId(), question.getStudentId(), question.getUserId(),
                question.getExpertCategoryId(), question.getExpertId(), question.getTitle(), question.getContent(),
                question.getStatus(), qaStatusCode(question.getStatus()), qaStatusLabel(question.getStatus()),
                includeAnswers ? loadAnswers(question.getId()) : List.of());
    }

    private List<QaAnswerResponse> loadAnswers(Long questionId) {
        return qaAnswerMapper.selectList(new LambdaQueryWrapper<QaAnswer>()
                        .eq(QaAnswer::getDeleted, 0)
                        .eq(QaAnswer::getQuestionId, questionId)
                        .orderByAsc(QaAnswer::getAnsweredAt))
                .stream()
                .map(this::toQaAnswerResponse)
                .toList();
    }

    private QaAnswerResponse toQaAnswerResponse(QaAnswer answer) {
        return new QaAnswerResponse(answer.getId(), answer.getQuestionId(), answer.getAdminId(),
                answer.getExpertId(), answer.getContent(), answer.getAnsweredAt());
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        AppUser user = feedback.getUserId() == null ? null : appUserMapper.selectById(feedback.getUserId());
        return new FeedbackResponse(feedback.getId(), feedback.getUserId(), feedback.getStudentId(),
                user == null ? null : user.getNickname(),
                user == null ? null : appUserAvatarUrlResolver.resolve(user.getId(), user.getAvatarUrl()),
                user == null ? null : user.getMobile(),
                feedback.getFeedbackType(), feedback.getContent(), feedback.getContact(), feedback.getStatus(),
                feedback.getProcessedBy(), feedback.getProcessedAt(), feedback.getProcessNote(), feedback.getCreatedAt());
    }

    private void saveAudit(String targetType, Long targetId, Integer before, Integer after, String comment) {
        AuditRecord record = new AuditRecord();
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setBeforeStatus(before);
        record.setAfterStatus(after);
        record.setAuditComment(comment);
        record.setAuditorId(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        record.setAuditedAt(LocalDateTime.now());
        auditRecordService.save(record);
    }

    private <E, R> PageResponse<R> pageResponse(Page<E> page, List<R> records) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String qaStatusCode(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "pending";
            case ANSWERED -> "answered";
            case CLOSED -> "closed";
        };
    }

    private String qaStatusLabel(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "待回复";
            case ANSWERED -> "已回复";
            case CLOSED -> "已关闭";
        };
    }
}
